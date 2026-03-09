/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.sensor

import com.facebook.battery.metrics.sensor.SensorMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class SensorMetricsReporter : SystemMetricsReporter<SensorMetrics> {

  override fun reportTo(metrics: SensorMetrics, event: SystemMetricsReporter.Event) {
    if (metrics.total.powerMah != 0.0) {
      event.add(TOTAL_POWER_MAH, metrics.total.powerMah)
    }

    if (metrics.total.activeTimeMs != 0L) {
      event.add(TOTAL_ACTIVE_TIME_MS, metrics.total.activeTimeMs)
    }

    if (metrics.total.wakeUpTimeMs != 0L) {
      event.add(TOTAL_WAKEUP_TIME_MS, metrics.total.wakeUpTimeMs)
    }
  }

  companion object {
    const val TOTAL_POWER_MAH: String = "sensor_power_mah"
    const val TOTAL_ACTIVE_TIME_MS: String = "sensor_active_time_ms"
    const val TOTAL_WAKEUP_TIME_MS: String = "sensor_wakeup_time_ms"
  }
}
