/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.bluetooth

import com.facebook.battery.metrics.bluetooth.BluetoothMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class BluetoothMetricsSerializer : SystemMetricsSerializer<BluetoothMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: BluetoothMetrics, output: DataOutput) {
    output.writeInt(metrics.bleScanCount)
    output.writeLong(metrics.bleScanDurationMs)
    output.writeInt(metrics.bleOpportunisticScanCount)
    output.writeLong(metrics.bleOpportunisticScanDurationMs)
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: BluetoothMetrics, input: DataInput): Boolean {
    metrics.bleScanCount = input.readInt()
    metrics.bleScanDurationMs = input.readLong()
    metrics.bleOpportunisticScanCount = input.readInt()
    metrics.bleOpportunisticScanDurationMs = input.readLong()
    return true
  }

  companion object {
    private const val serialVersionUID = -4_085_774_432_413_599_882L
  }
}
