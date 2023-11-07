/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.devicebattery

import com.facebook.battery.metrics.devicebattery.DeviceBatteryMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class DeviceBatteryMetricsSerializer : SystemMetricsSerializer<DeviceBatteryMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: DeviceBatteryMetrics, output: DataOutput) {
    output.writeFloat(metrics.batteryLevelPct)
    output.writeLong(metrics.batteryRealtimeMs)
    output.writeLong(metrics.chargingRealtimeMs)
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: DeviceBatteryMetrics, input: DataInput): Boolean {
    metrics.batteryLevelPct = input.readFloat()
    metrics.batteryRealtimeMs = input.readLong()
    metrics.chargingRealtimeMs = input.readLong()
    return true
  }

  companion object {
    private const val serialVersionUID = -2_269_842_438_411_178_483L
  }
}
