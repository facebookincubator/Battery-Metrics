/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.memory

import com.facebook.battery.metrics.core.SystemMetrics
import kotlin.jvm.JvmField

class MemoryMetrics : SystemMetrics<MemoryMetrics?>() {

  // Memory metrics takes snapshot of current state of memory, since the memory is not
  // monotonically increasing the sequence number to track the freshness. The sum()/diff()
  // method will always return the latest snapshot as result.
  @JvmField var sequenceNumber: Long = 0

  @JvmField var javaHeapMaxSizeKb: Long = 0

  @JvmField var javaHeapAllocatedKb: Long = 0

  @JvmField var nativeHeapSizeKb: Long = 0

  @JvmField var nativeHeapAllocatedKb: Long = 0

  @JvmField var vmSizeKb: Long = 0

  @JvmField var vmRssKb: Long = 0

  override fun set(metrics: MemoryMetrics): MemoryMetrics {
    this.javaHeapMaxSizeKb = metrics.javaHeapMaxSizeKb
    this.javaHeapAllocatedKb = metrics.javaHeapAllocatedKb
    this.nativeHeapSizeKb = metrics.nativeHeapSizeKb
    this.nativeHeapAllocatedKb = metrics.nativeHeapAllocatedKb
    this.vmSizeKb = metrics.vmSizeKb
    this.vmRssKb = metrics.vmRssKb
    return this
  }

  override fun sum(b: MemoryMetrics?, output: MemoryMetrics?): MemoryMetrics {
    var output = output
    if (output == null) {
      output = MemoryMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      val latest = if (sequenceNumber > b.sequenceNumber) this else b
      output.sequenceNumber = latest.sequenceNumber
      output.javaHeapMaxSizeKb = latest.javaHeapMaxSizeKb
      output.javaHeapAllocatedKb = latest.javaHeapAllocatedKb
      output.nativeHeapSizeKb = latest.nativeHeapSizeKb
      output.nativeHeapAllocatedKb = latest.nativeHeapAllocatedKb
      output.vmSizeKb = latest.vmSizeKb
      output.vmRssKb = latest.vmRssKb
    }

    return output
  }

  override fun diff(b: MemoryMetrics?, output: MemoryMetrics?): MemoryMetrics {
    var output = output
    if (output == null) {
      output = MemoryMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      val latest = if (sequenceNumber >= b.sequenceNumber) this else b
      output.sequenceNumber = latest.sequenceNumber
      output.javaHeapMaxSizeKb = latest.javaHeapMaxSizeKb
      output.javaHeapAllocatedKb = latest.javaHeapAllocatedKb
      output.nativeHeapSizeKb = latest.nativeHeapSizeKb
      output.nativeHeapAllocatedKb = latest.nativeHeapAllocatedKb
      output.vmSizeKb = latest.vmSizeKb
      output.vmRssKb = latest.vmRssKb
    }

    return output
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) {
      return true
    }
    if (o == null || javaClass != o.javaClass) {
      return false
    }

    val that = o as MemoryMetrics

    return javaHeapMaxSizeKb == that.javaHeapMaxSizeKb &&
        javaHeapAllocatedKb == that.javaHeapAllocatedKb &&
        nativeHeapSizeKb == that.nativeHeapSizeKb &&
        nativeHeapAllocatedKb == that.nativeHeapAllocatedKb &&
        vmSizeKb == that.vmSizeKb &&
        vmRssKb == that.vmRssKb
  }

  override fun hashCode(): Int {
    var result = (javaHeapMaxSizeKb xor (javaHeapMaxSizeKb ushr 32)).toInt()
    result = 31 * result + (javaHeapAllocatedKb xor (javaHeapAllocatedKb ushr 32)).toInt()
    result = 31 * result + (nativeHeapSizeKb xor (nativeHeapSizeKb ushr 32)).toInt()
    result = 31 * result + (nativeHeapAllocatedKb xor (nativeHeapAllocatedKb ushr 32)).toInt()
    result = 31 * result + (vmSizeKb xor (vmSizeKb ushr 32)).toInt()
    result = 31 * result + (vmRssKb xor (vmRssKb ushr 32)).toInt()

    return result
  }

  override fun toString(): String =
      "MemoryMetrics{javaHeapMaxSizeKb=${javaHeapMaxSizeKb}, javaHeapAllocatedKb=${javaHeapAllocatedKb}, nativeHeapSizeKb=${nativeHeapSizeKb}, nativeHeapAllocatedKb=${nativeHeapAllocatedKb}, vmSizeKb=${vmSizeKb}, vmRssKb=${vmRssKb}}"
}
