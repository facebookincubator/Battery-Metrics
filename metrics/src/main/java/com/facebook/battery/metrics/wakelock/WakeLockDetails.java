/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.wakelock;

import android.os.PowerManager;
import android.os.SystemClock;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import java.lang.ref.WeakReference;

/**
 * A simple container to maintain wakelock information.
 *
 * <p>The Wakelock API only maintains one timeout at a time -- releasing a wakelock kills any
 * pending timeouts, and setting a new timeout simply overrides the previous one. This class mimics
 * that behavior.
 *
 * <p>This is very tightly coupled with the WakelockMetrics collector and helps maintain global
 * state about how many wakelocks are currently active to maintain an overall wakelock acquired
 * time.
 */
/* package */ class WakeLockDetails {
  public final String tag;
  public final int levelAndFlags;
  public final WeakReference<PowerManager.WakeLock> wakeLockReference;

  private boolean mIsReferenceCounted = true;
  private boolean mIsHeld = false;
  private int mReferences = 0;
  private long mAcquireTimeMs;
  private long mTimeoutTimeMs = Long.MAX_VALUE;
  private long mHeldTimeMs;
  private long mLastReleasedMs = -1;

  public WakeLockDetails(PowerManager.WakeLock wakeLockReference, String tag, int levelAndFlags) {
    this.tag = tag;
    this.levelAndFlags = levelAndFlags;
    this.wakeLockReference = new WeakReference<>(wakeLockReference);
  }

  /**
   * Returns false if the wakelock was already acquired, and true if this actually acquired the
   * wakelock.
   */
  public boolean acquire(long timeoutMs) {
    long currentTimeMs = SystemClock.uptimeMillis();
    if (timeoutMs >= 0) {
      mTimeoutTimeMs = currentTimeMs + timeoutMs;
    }

    // Mimic {@link PowerManager$WakeLock.acquireLocked
    if (mIsReferenceCounted && mReferences++ != 0) {
      return false;
    } else if (mIsHeld) {
      return false;
    }

    mAcquireTimeMs = currentTimeMs;
    mIsHeld = true;
    return true;
  }

  /**
   * Returns the time the wakelock was released at; -1 if the wakelock hasn't been released yet, and
   * an actual timestamp if it's been released.
   */
  public boolean release() {
    return releaseAtTime(SystemClock.uptimeMillis());
  }

  private boolean releaseAtTime(long releaseTimeMs) {
    // Reference counted wakelocks will throw an exception in the PowerManager,
    // non-reference counted wakelocks will just work without changes.
    if (!mIsHeld) {
      return false;
    }

    // Mimic {@link PowerManager.WakeLock#release()}
    if (mIsReferenceCounted && --mReferences != 0) {
      return false;
    }

    mLastReleasedMs = releaseTimeMs;
    mHeldTimeMs += releaseTimeMs - mAcquireTimeMs;
    mTimeoutTimeMs = Long.MAX_VALUE;
    mIsHeld = false;

    return true;
  }

  public boolean applyAutomaticReleases() {
    boolean timeoutReleaseTimeMs = applyTimeouts();
    boolean finalizeReleaseTimeMs = applyFinalize();
    return timeoutReleaseTimeMs || finalizeReleaseTimeMs;
  }

  private boolean applyTimeouts() {
    long currentTimeMs = SystemClock.uptimeMillis();
    if (currentTimeMs >= mTimeoutTimeMs) {
      return releaseAtTime(mTimeoutTimeMs);
    }

    return false;
  }

  private boolean applyFinalize() {
    if (mIsHeld && wakeLockReference.get() == null) { // Leaked wakelock
      SystemMetricsLogger.wtf(
          "WakeLockMetricsCollector",
          "The wakelock " + tag + " was garbage collected before being released.");
      return releaseAtTime(SystemClock.uptimeMillis());
    }

    return false;
  }

  public long getHeldTimeMs() {
    return mHeldTimeMs + (mIsHeld ? (SystemClock.uptimeMillis() - mAcquireTimeMs) : 0);
  }

  public long getLastReleaseTimeMs() {
    return mLastReleasedMs;
  }

  public boolean isHeld() {
    return mIsHeld;
  }

  public WakeLockDetails setIsReferenceCounted(boolean isReferenceCounted) {
    this.mIsReferenceCounted = isReferenceCounted;
    return this;
  }
}
