/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.sensor

import com.facebook.battery.metrics.sensor.SensorMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class SensorMetricsSerializer : SystemMetricsSerializer<SensorMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: SensorMetrics, output: DataOutput) {
    output.writeDouble(metrics.total.powerMah)
    output.writeLong(metrics.total.activeTimeMs)
    output.writeLong(metrics.total.wakeUpTimeMs)
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: SensorMetrics, input: DataInput): Boolean {
    metrics.total.powerMah = input.readDouble()
    metrics.total.activeTimeMs = input.readLong()
    metrics.total.wakeUpTimeMs = input.readLong()
    return true
  }

  companion object {
    private const val serialVersionUID = 1_224L
  }
}
