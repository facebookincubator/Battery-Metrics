/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.memory

import com.facebook.battery.metrics.memory.MemoryMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class MemoryMetricsSerializer : SystemMetricsSerializer<MemoryMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: MemoryMetrics, output: DataOutput) {
    output.writeLong(metrics.javaHeapMaxSizeKb)
    output.writeLong(metrics.javaHeapAllocatedKb)
    output.writeLong(metrics.nativeHeapSizeKb)
    output.writeLong(metrics.nativeHeapAllocatedKb)
    output.writeLong(metrics.vmSizeKb)
    output.writeLong(metrics.vmRssKb)
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: MemoryMetrics, input: DataInput): Boolean {
    metrics.javaHeapMaxSizeKb = input.readLong()
    metrics.javaHeapAllocatedKb = input.readLong()
    metrics.nativeHeapSizeKb = input.readLong()
    metrics.nativeHeapAllocatedKb = input.readLong()
    metrics.vmSizeKb = input.readLong()
    metrics.vmRssKb = input.readLong()
    return true
  }

  companion object {
    private const val serialVersionUID = -4_040_221_479_651_313_008L
  }
}
