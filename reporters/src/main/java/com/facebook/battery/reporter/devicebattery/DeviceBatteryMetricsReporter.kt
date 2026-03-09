/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.devicebattery

import com.facebook.battery.metrics.devicebattery.DeviceBatteryMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class DeviceBatteryMetricsReporter : SystemMetricsReporter<DeviceBatteryMetrics> {

  override fun reportTo(metrics: DeviceBatteryMetrics, event: SystemMetricsReporter.Event) {
    event.add(BATTERY_PCT, metrics.batteryLevelPct.toDouble())
    event.add(BATTERY_REALTIME_MS, metrics.batteryRealtimeMs)
    event.add(CHARGING_REALTIME_MS, metrics.chargingRealtimeMs)
  }

  companion object {
    const val BATTERY_PCT: String = "battery_pct"
    const val BATTERY_REALTIME_MS: String = "battery_realtime_ms"
    const val CHARGING_REALTIME_MS: String = "charging_realtime_ms"
  }
}
