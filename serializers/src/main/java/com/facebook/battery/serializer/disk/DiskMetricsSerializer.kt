/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.disk

import com.facebook.battery.metrics.disk.DiskMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class DiskMetricsSerializer : SystemMetricsSerializer<DiskMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: DiskMetrics, output: DataOutput) {
    output.writeLong(metrics.rcharBytes)
    output.writeLong(metrics.wcharBytes)
    output.writeLong(metrics.syscrCount)
    output.writeLong(metrics.syscwCount)
    output.writeLong(metrics.readBytes)
    output.writeLong(metrics.writeBytes)
    output.writeLong(metrics.cancelledWriteBytes)
    output.writeLong(metrics.majorFaults)
    output.writeLong(metrics.blkIoTicks)
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: DiskMetrics, input: DataInput): Boolean {
    metrics.rcharBytes = input.readLong()
    metrics.wcharBytes = input.readLong()
    metrics.syscrCount = input.readLong()
    metrics.syscwCount = input.readLong()
    metrics.readBytes = input.readLong()
    metrics.writeBytes = input.readLong()
    metrics.cancelledWriteBytes = input.readLong()
    metrics.majorFaults = input.readLong()
    metrics.blkIoTicks = input.readLong()
    return true
  }

  companion object {
    private const val serialVersionUID = -3_940_877_017_738_808_059L
  }
}
