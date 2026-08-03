/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.devicebattery

import com.facebook.battery.metrics.core.SystemMetrics

/**
 * This class contains the metrics for measuring the device level battery metrics. This is cheap to
 * record and can be a useful addition to modeled battery drain. We also measure the time spent on
 * battery and while charging since the start of the app
 */
class DeviceBatteryMetrics : SystemMetrics<DeviceBatteryMetrics>() {

  // Device battery level
  @JvmField var batteryLevelPct: Float = 0f

  // Elapsed Realtime on battery since the start of the app
  @JvmField var batteryRealtimeMs: Long = 0

  // Elapsed realtime while charging since the start of the app
  @JvmField var chargingRealtimeMs: Long = 0

  override fun set(metrics: DeviceBatteryMetrics): DeviceBatteryMetrics {
    this.batteryLevelPct = metrics.batteryLevelPct
    this.batteryRealtimeMs = metrics.batteryRealtimeMs
    this.chargingRealtimeMs = metrics.chargingRealtimeMs
    return this
  }

  override fun sum(
      b: DeviceBatteryMetrics?,
      output: DeviceBatteryMetrics?,
  ): DeviceBatteryMetrics {
    var output = output
    if (output == null) {
      output = DeviceBatteryMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      output.batteryLevelPct = batteryLevelPct + b.batteryLevelPct
      output.batteryRealtimeMs = batteryRealtimeMs + b.batteryRealtimeMs
      output.chargingRealtimeMs = chargingRealtimeMs + b.chargingRealtimeMs
    }
    return output
  }

  override fun diff(
      b: DeviceBatteryMetrics?,
      output: DeviceBatteryMetrics?,
  ): DeviceBatteryMetrics {
    var output = output
    if (output == null) {
      output = DeviceBatteryMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      output.batteryLevelPct = batteryLevelPct - b.batteryLevelPct
      output.batteryRealtimeMs = batteryRealtimeMs - b.batteryRealtimeMs
      output.chargingRealtimeMs = chargingRealtimeMs - b.chargingRealtimeMs
    }

    return output
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null || javaClass != other.javaClass) {
      return false
    }

    val that = other as DeviceBatteryMetrics

    if (batteryLevelPct != that.batteryLevelPct) {
      return false
    }
    if (batteryRealtimeMs != that.batteryRealtimeMs) {
      return false
    }
    return chargingRealtimeMs == that.chargingRealtimeMs
  }

  override fun hashCode(): Int {
    var result =
        if (this.batteryLevelPct != +0.0f) java.lang.Float.floatToIntBits(this.batteryLevelPct)
        else 0
    result = 31 * result + (batteryRealtimeMs xor (batteryRealtimeMs ushr 32)).toInt()
    result = 31 * result + (chargingRealtimeMs xor (chargingRealtimeMs ushr 32)).toInt()
    return result
  }

  override fun toString(): String =
      "DeviceBatteryMetrics{batteryLevelPct=${batteryLevelPct}, batteryRealtimeMs=${batteryRealtimeMs}, chargingRealtimeMs=${chargingRealtimeMs}${'}'}"

  companion object {
    private const val serialVersionUID = 1L
  }
}
