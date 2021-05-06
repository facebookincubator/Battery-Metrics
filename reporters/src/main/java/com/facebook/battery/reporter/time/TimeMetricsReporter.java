/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.time;

import com.facebook.battery.metrics.time.TimeMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class TimeMetricsReporter implements SystemMetricsReporter<TimeMetrics> {

  public static final String REALTIME_MS = "realtime_ms";
  public static final String UPTIME_MS = "uptime_ms";

  @Override
  public void reportTo(TimeMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.realtimeMs != 0) {
      event.add(REALTIME_MS, metrics.realtimeMs);
    }

    if (metrics.uptimeMs != 0) {
      event.add(UPTIME_MS, metrics.uptimeMs);
    }
  }
}
