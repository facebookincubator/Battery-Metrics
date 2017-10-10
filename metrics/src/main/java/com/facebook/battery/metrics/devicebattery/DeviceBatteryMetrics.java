/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.devicebattery;

import android.support.annotation.Nullable;
import com.facebook.battery.metrics.api.SystemMetrics;

/** Information about device level battery usage */
public class DeviceBatteryMetrics extends SystemMetrics<DeviceBatteryMetrics> {

  private static final long serialVersionUID = 0;

  public float batteryLevelPct;
  public long batteryRealtimeMs;
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
  public boolean equals(Object o) {
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
