/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.devicebattery;

import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.infer.annotation.Nullsafe;

/**
 * This class contains the metrics for measuring the device level battery metrics. This is cheap to
 * record and can be a useful addition to modeled battery drain. We also measure the time spent on
 * battery and while charging since the start of the app
 */
@Nullsafe(Nullsafe.Mode.RUNTIME)
public class DeviceBatteryMetrics extends SystemMetrics<DeviceBatteryMetrics> {

  // Device battery level
  public float batteryLevelPct;
  // Elapsed Realtime on battery since the start of the app
  public long batteryRealtimeMs;
  // Elapsed realtime while charging since the start of the app
  public long chargingRealtimeMs;

  public DeviceBatteryMetrics() {}

  @Override
  public DeviceBatteryMetrics set(DeviceBatteryMetrics metrics) {
    batteryLevelPct = metrics.batteryLevelPct;
    batteryRealtimeMs = metrics.batteryRealtimeMs;
    chargingRealtimeMs = metrics.chargingRealtimeMs;
    return this;
  }

  @Override
  public DeviceBatteryMetrics sum(
      @Nullable DeviceBatteryMetrics b, @Nullable DeviceBatteryMetrics output) {
    if (output == null) {
      output = new DeviceBatteryMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.batteryLevelPct = batteryLevelPct + b.batteryLevelPct;
      output.batteryRealtimeMs = batteryRealtimeMs + b.batteryRealtimeMs;
      output.chargingRealtimeMs = chargingRealtimeMs + b.chargingRealtimeMs;
    }
    return output;
  }

  @Override
  public DeviceBatteryMetrics diff(
      @Nullable DeviceBatteryMetrics b, @Nullable DeviceBatteryMetrics output) {
    if (output == null) {
      output = new DeviceBatteryMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.batteryLevelPct = batteryLevelPct - b.batteryLevelPct;
      output.batteryRealtimeMs = batteryRealtimeMs - b.batteryRealtimeMs;
      output.chargingRealtimeMs = chargingRealtimeMs - b.chargingRealtimeMs;
    }

    return output;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DeviceBatteryMetrics that = (DeviceBatteryMetrics) o;

    if (batteryLevelPct != that.batteryLevelPct) {
      return false;
    }
    if (batteryRealtimeMs != that.batteryRealtimeMs) {
      return false;
    }
    return chargingRealtimeMs == that.chargingRealtimeMs;
  }

  @Override
  public int hashCode() {
    int result = (batteryLevelPct != +0.0f ? Float.floatToIntBits(batteryLevelPct) : 0);
    result = 31 * result + (int) (batteryRealtimeMs ^ (batteryRealtimeMs >>> 32));
    result = 31 * result + (int) (chargingRealtimeMs ^ (chargingRealtimeMs >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "DeviceBatteryMetrics{"
        + "batteryLevelPct="
        + batteryLevelPct
        + ", batteryRealtimeMs="
        + batteryRealtimeMs
        + ", chargingRealtimeMs="
        + chargingRealtimeMs
        + '}';
  }
}
