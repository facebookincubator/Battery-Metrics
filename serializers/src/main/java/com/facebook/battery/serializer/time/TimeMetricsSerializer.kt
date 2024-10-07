/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.time

import com.facebook.battery.metrics.time.TimeMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class TimeMetricsSerializer : SystemMetricsSerializer<TimeMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: TimeMetrics, output: DataOutput) {
    output.writeLong(metrics.realtimeMs)
    output.writeLong(metrics.uptimeMs)
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: TimeMetrics, input: DataInput): Boolean {
    metrics.realtimeMs = input.readLong()
    metrics.uptimeMs = input.readLong()
    return true
  }

  companion object {
    private const val serialVersionUID = 4_345_974_300_167_284_411L
  }
}
