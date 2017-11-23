/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.time;

import static com.facebook.battery.metrics.core.Utilities.checkNotNull;

import android.os.SystemClock;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;

/**
 * Records system uptime (doesn't include time when the phone was asleep) and realtime (actual time
 * elapsed). This is a fairly core collector that can be used to normalize the values obtained by
 * all other collectors for comparison.
 */
@ThreadSafe
public class TimeMetricsCollector extends SystemMetricsCollector<TimeMetrics> {

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(TimeMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");
    snapshot.realtimeMs = SystemClock.elapsedRealtime();
    snapshot.uptimeMs = SystemClock.uptimeMillis();
    return true;
  }

  @Override
  public TimeMetrics createMetrics() {
    return new TimeMetrics();
  }
}
