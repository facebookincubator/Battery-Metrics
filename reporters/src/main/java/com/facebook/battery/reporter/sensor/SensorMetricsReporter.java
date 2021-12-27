/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.sensor;

import com.facebook.battery.metrics.sensor.SensorMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class SensorMetricsReporter implements SystemMetricsReporter<SensorMetrics> {

  public static final String TOTAL_POWER_MAH = "sensor_power_mah";
  public static final String TOTAL_ACTIVE_TIME_MS = "sensor_active_time_ms";
  public static final String TOTAL_WAKEUP_TIME_MS = "sensor_wakeup_time_ms";

  @Override
  public void reportTo(SensorMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.total.powerMah != 0) {
      event.add(TOTAL_POWER_MAH, metrics.total.powerMah);
    }

    if (metrics.total.activeTimeMs != 0) {
      event.add(TOTAL_ACTIVE_TIME_MS, metrics.total.activeTimeMs);
    }

    if (metrics.total.wakeUpTimeMs != 0) {
      event.add(TOTAL_WAKEUP_TIME_MS, metrics.total.wakeUpTimeMs);
    }
  }
}
