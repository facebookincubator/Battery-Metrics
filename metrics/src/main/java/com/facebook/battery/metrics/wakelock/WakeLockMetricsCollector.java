/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.wakelock;

import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.util.SimpleArrayMap;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.WeakHashMap;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Records information about currently active wakelocks, including attribution by tag.
 *
 * <p>Snapshots can be attributed (at the cost of extra memory) or unattributed, in which case the
 * collector will only record total wakelock time instead.
 *
 * <p>This must be instrumented by hand as well, with - {@link PowerManager#newWakeLock(int,
 * String)} -> {@link #newWakeLock(PowerManager.WakeLock, int, String)} - {@link
 * PowerManager.WakeLock#acquire()}, {@link PowerManager.WakeLock#acquire(long)} -> {@link
 * #acquire(PowerManager.WakeLock, long)} - {@link PowerManager.WakeLock#release()}, {@link
 * PowerManager.WakeLock#release()} -> {@link #release(PowerManager.WakeLock, int)} - {@link
 * PowerManager.WakeLock#setReferenceCounted(boolean)} -> {@link
 * #setReferenceCounted(PowerManager.WakeLock, boolean)}
 */
@ThreadSafe
public class WakeLockMetricsCollector extends SystemMetricsCollector<WakeLockMetrics> {

  private static final String TAG = "WakeLockMetricsCollector";

  private final WeakHashMap<PowerManager.WakeLock, WakeLockDetails> mWakeLocks =
      new WeakHashMap<>();

  /** Time for garbage collected wakelocks */
  private final SimpleArrayMap<String, Long> mPrevWakeLockMs = new SimpleArrayMap<>();

  /** Details about currently active wakelocks */
  private final ArrayList<WakeLockDetails> mActiveWakeLockDetails = new ArrayList<>();

  private long mWakeLockAcquireTimeMs;
  private long mWakeLocksHeldTimeMs;
  private int mActiveWakeLocks;

  public synchronized void newWakeLock(
      PowerManager.WakeLock wakelock, int levelAndFlags, String tag) {
    WakeLockDetails details = new WakeLockDetails(wakelock, tag, levelAndFlags);
    mWakeLocks.put(wakelock, details);
    mActiveWakeLockDetails.add(details);
  }

  public synchronized void acquire(PowerManager.WakeLock wakelock, long timeout) {
    updateWakeLockCounts();
    WakeLockDetails details = mWakeLocks.get(wakelock);
    if (details == null) {
      SystemMetricsLogger.wtf(TAG, "Unknown wakelock modified");
      return;
    }

    if (details.acquire(timeout)) {
      if (mActiveWakeLocks == 0) {
        mWakeLockAcquireTimeMs = SystemClock.uptimeMillis();
      }

      mActiveWakeLocks++;
    }
  }

  public synchronized void release(PowerManager.WakeLock wakelock, int flags) {
    updateWakeLockCounts();

    WakeLockDetails details = mWakeLocks.get(wakelock);
    if (details == null) {
      SystemMetricsLogger.wtf(TAG, "Unknown wakelock modified");
      return;
    }

    if (details.release()) {
      mActiveWakeLocks--;

      if (mActiveWakeLocks == 0) {
        mWakeLocksHeldTimeMs += details.getLastReleaseTimeMs() - mWakeLockAcquireTimeMs;
      }
    }
  }

  public synchronized void setReferenceCounted(PowerManager.WakeLock wakelock, boolean value) {
    WakeLockDetails details = mWakeLocks.get(wakelock);
    if (details == null) {
      SystemMetricsLogger.wtf(TAG, "Unknown wakelock modified");
      return;
    }

    details.setIsReferenceCounted(value);
  }

  /**
   * Takes care of releasing wakelocks that might have changed since the last check, and
   * consolidating time wakelocks have been active: this function takes care of releasing wakelocks
   * automatically either by timeouts or by garbage collection.
   */
  private synchronized void updateWakeLockCounts() {
    int activeWakeLocks = 0;
    long maxReleaseTimeMs = -1;

    for (Iterator<WakeLockDetails> iter = mActiveWakeLockDetails.iterator(); iter.hasNext(); ) {
      WakeLockDetails details = iter.next();

      boolean released = details.applyAutomaticReleases();
      if (details.isHeld()) {
        activeWakeLocks++;
      } else if (released && details.getLastReleaseTimeMs() > maxReleaseTimeMs) {
        maxReleaseTimeMs = details.getLastReleaseTimeMs();
      }

      // Garbage collect WakeLockDetails to aggregate over the tag name instead, setting a more
      // reasonable upper bound on the amount of data we keep in memory.
      if (details.wakeLockReference.get() == null) {
        Long existingValue = mPrevWakeLockMs.get(details.tag);
        mPrevWakeLockMs.put(
            details.tag, (existingValue == null ? 0 : existingValue) + details.getHeldTimeMs());
        iter.remove();
      }
    }

    if (mActiveWakeLocks != 0 && activeWakeLocks == 0) {
      mWakeLocksHeldTimeMs += maxReleaseTimeMs - mWakeLockAcquireTimeMs;
    }
    mActiveWakeLocks = activeWakeLocks;
  }

  @Override
  public synchronized boolean getSnapshot(WakeLockMetrics snapshot) {
    updateWakeLockCounts();

    snapshot.heldTimeMs =
        mWakeLocksHeldTimeMs
            + (mActiveWakeLocks > 0 ? SystemClock.uptimeMillis() - mWakeLockAcquireTimeMs : 0);
    if (snapshot.isAttributionEnabled) {
      snapshot.tagTimeMs.clear();
    }

    for (int i = 0, size = mActiveWakeLockDetails.size(); i < size; i++) {
      WakeLockDetails details = mActiveWakeLockDetails.get(i);
      long heldTimeMs = details.getHeldTimeMs();

      if (snapshot.isAttributionEnabled) {
        String tag = details.tag;
        Long existingValue = snapshot.tagTimeMs.get(tag);
        snapshot.tagTimeMs.put(tag, (existingValue == null ? 0 : existingValue) + heldTimeMs);
      }
    }

    for (int i = 0, size = mPrevWakeLockMs.size(); i < size; i++) {
      String tag = mPrevWakeLockMs.keyAt(i);
      Long existingValue = snapshot.tagTimeMs.get(tag);
      snapshot.tagTimeMs.put(
          tag, (existingValue == null ? 0 : existingValue) + mPrevWakeLockMs.valueAt(i));
    }

    return true;
  }

  @Override
  public WakeLockMetrics createMetrics() {
    return new WakeLockMetrics();
  }
}
