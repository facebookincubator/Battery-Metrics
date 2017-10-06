// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.time;

import android.os.SystemClock;
import com.facebook.battery.metrics.api.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;

@ThreadSafe
public class TimeMetricsCollector extends SystemMetricsCollector<TimeMetrics> {

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(TimeMetrics snapshot) {
    snapshot.realtimeMs = SystemClock.elapsedRealtime();
    snapshot.uptimeMs = SystemClock.uptimeMillis();
    return true;
  }

  @Override
  public TimeMetrics createMetrics() {
    return new TimeMetrics();
  }
}
