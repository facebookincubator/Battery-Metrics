/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.devicebattery;

import com.facebook.battery.metrics.devicebattery.DeviceBatteryMetrics;
import com.facebook.battery.reporter.api.SystemMetricsReporter;

public class DeviceBatteryMetricsReporter implements SystemMetricsReporter<DeviceBatteryMetrics> {

  public static final String BATTERY_PCT = "battery_pct";
  public static final String BATTERY_REALTIME_MS = "battery_realtime_ms";
  public static final String CHARGING_REALTIME_MS = "charging_realtime_ms";

  @Override
  public void reportTo(DeviceBatteryMetrics metrics, SystemMetricsReporter.Event event) {
    event.add(BATTERY_PCT, metrics.batteryLevelPct);
    event.add(BATTERY_REALTIME_MS, metrics.batteryRealtimeMs);
    event.add(CHARGING_REALTIME_MS, metrics.chargingRealtimeMs);
  }
}
