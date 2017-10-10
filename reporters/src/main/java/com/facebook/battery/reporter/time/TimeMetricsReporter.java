/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.time;

import com.facebook.battery.metrics.time.TimeMetrics;
import com.facebook.battery.reporter.api.SystemMetricsReporter;

public class TimeMetricsReporter implements SystemMetricsReporter<TimeMetrics> {

  public static final String REALTIME_MS = "realtime_ms";
  public static final String UPTIME_MS = "uptime_ms";

  @Override
  public void reportTo(TimeMetrics metrics, Event event) {
    if (metrics.realtimeMs != 0) {
      event.add(REALTIME_MS, metrics.realtimeMs);
    }

    if (metrics.uptimeMs != 0) {
      event.add(UPTIME_MS, metrics.uptimeMs);
    }
  }
}
