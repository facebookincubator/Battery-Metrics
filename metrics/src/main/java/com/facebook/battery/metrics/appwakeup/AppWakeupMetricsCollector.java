/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.appwakeup;

import static com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupDetails;
import static com.facebook.battery.metrics.core.Utilities.checkNotNull;

import android.os.SystemClock;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.infer.annotation.ThreadSafe;

/**
 * This class is used to record and aggregate the wakeups in the app. It exposes methods to record
 * start {@link #recordWakeupStart(AppWakeupMetrics.WakeupReason, String)} and end {@link
 * #recordWakeupEnd(String)} of a wakeup
 */
@ThreadSafe
public class AppWakeupMetricsCollector extends SystemMetricsCollector<AppWakeupMetrics> {
  private static final String TAG = "AppWakeupMetricsCollector";

  private final AppWakeupMetrics mMetrics;
  private final AppWakeupMetrics mRunningWakeups;

  public AppWakeupMetricsCollector() {
    mMetrics = new AppWakeupMetrics();
    mRunningWakeups = new AppWakeupMetrics();
  }

  @Override
  @ThreadSafe(enableChecks = false)
  public synchronized boolean getSnapshot(AppWakeupMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");
    // TODO: Optimize by taking intersection of the two lists
    snapshot.appWakeups.clear();
    for (int i = 0; i < mMetrics.appWakeups.size(); i++) {
      WakeupDetails details = new WakeupDetails();
      details.set(mMetrics.appWakeups.valueAt(i));
      snapshot.appWakeups.put(mMetrics.appWakeups.keyAt(i), details);
    }
    return true;
  }

  @Override
  public AppWakeupMetrics createMetrics() {
    return new AppWakeupMetrics();
  }

  /**
   * Record the start of a wakeup. If this method is called twice without calling recordWakeupEnd in
   * between, we ignore the second recordWakeupStart call.
   *
   * @param reason One of the wakeup types defined in {@link AppWakeupMetrics.WakeupReason}
   * @param id Identifier of the wakeup
   */
  public synchronized void recordWakeupStart(AppWakeupMetrics.WakeupReason reason, String id) {
    if (mRunningWakeups.appWakeups.containsKey(id)) {
      // This condition is possible depending on the usage of tags. We can see from this soft
      // error if this condition is indeed occurring in our codebase.
      SystemMetricsLogger.wtf(
          TAG, "Wakeup started again without ending for " + id + " (" + reason + ")");
      return;
    }
    mRunningWakeups.appWakeups.put(
        id, new AppWakeupMetrics.WakeupDetails(reason, 1, SystemClock.elapsedRealtime()));
  }

  /**
   * Record the end of a wakeup. If this method is called without calling a corresponding
   * recordWakeupStart, then we do nothing.
   *
   * @param id Identifier of the wakeup
   */
  public synchronized void recordWakeupEnd(String id) {
    if (!mRunningWakeups.appWakeups.containsKey(id)) {
      return;
    }
    AppWakeupMetrics.WakeupDetails details = mRunningWakeups.appWakeups.get(id);
    details.wakeupTimeMs = SystemClock.elapsedRealtime() - details.wakeupTimeMs;
    if (!mMetrics.appWakeups.containsKey(id)) {
      mMetrics.appWakeups.put(id, new AppWakeupMetrics.WakeupDetails().set(details));
    } else {
      mMetrics.appWakeups.get(id).sum(details, mMetrics.appWakeups.get(id));
    }
    mRunningWakeups.appWakeups.remove(id);
  }
}
