/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.disk

import com.facebook.battery.metrics.core.SystemMetrics

class DiskMetrics : SystemMetrics<DiskMetrics?>() {

  @JvmField var rcharBytes: Long = 0

  @JvmField var wcharBytes: Long = 0

  @JvmField var syscrCount: Long = 0

  @JvmField var syscwCount: Long = 0

  @JvmField var readBytes: Long = 0

  @JvmField var writeBytes: Long = 0

  @JvmField var cancelledWriteBytes: Long = 0

  @JvmField var majorFaults: Long = 0

  @JvmField var blkIoTicks: Long = 0

  override fun set(metrics: DiskMetrics): DiskMetrics {
    this.rcharBytes = metrics.rcharBytes
    this.wcharBytes = metrics.wcharBytes
    this.syscrCount = metrics.syscrCount
    this.syscwCount = metrics.syscwCount
    this.readBytes = metrics.readBytes
    this.writeBytes = metrics.writeBytes
    this.cancelledWriteBytes = metrics.cancelledWriteBytes
    this.majorFaults = metrics.majorFaults
    this.blkIoTicks = metrics.blkIoTicks
    return this
  }

  override fun sum(b: DiskMetrics?, output: DiskMetrics?): DiskMetrics {
    var output = output
    if (output == null) {
      output = DiskMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      output.rcharBytes = rcharBytes + b.rcharBytes
      output.wcharBytes = wcharBytes + b.wcharBytes
      output.syscrCount = syscrCount + b.syscrCount
      output.syscwCount = syscwCount + b.syscwCount
      output.readBytes = readBytes + b.readBytes
      output.writeBytes = writeBytes + b.writeBytes
      output.cancelledWriteBytes = cancelledWriteBytes + b.cancelledWriteBytes
      output.majorFaults = majorFaults + b.majorFaults
      output.blkIoTicks = blkIoTicks + b.blkIoTicks
    }

    return output
  }

  override fun diff(b: DiskMetrics?, output: DiskMetrics?): DiskMetrics {
    var output = output
    if (output == null) {
      output = DiskMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      output.rcharBytes = rcharBytes - b.rcharBytes
      output.wcharBytes = wcharBytes - b.wcharBytes
      output.syscrCount = syscrCount - b.syscrCount
      output.syscwCount = syscwCount - b.syscwCount
      output.readBytes = readBytes - b.readBytes
      output.writeBytes = writeBytes - b.writeBytes
      /* cancelledWriteBytes can be -ve if the file deleted before flushed from file cache */
      output.cancelledWriteBytes = cancelledWriteBytes - b.cancelledWriteBytes
      output.majorFaults = majorFaults - b.majorFaults
      output.blkIoTicks = blkIoTicks - b.blkIoTicks
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

    val that = other as DiskMetrics

    return that.rcharBytes == rcharBytes &&
        that.wcharBytes == wcharBytes &&
        that.syscrCount == syscrCount &&
        that.syscwCount == syscwCount &&
        that.readBytes == readBytes &&
        that.writeBytes == writeBytes &&
        that.cancelledWriteBytes == cancelledWriteBytes &&
        that.majorFaults == majorFaults &&
        that.blkIoTicks == blkIoTicks
  }

  override fun hashCode(): Int {
    var result = (rcharBytes xor (rcharBytes ushr 32)).toInt()
    result = 31 * result + (wcharBytes xor (wcharBytes ushr 32)).toInt()
    result = 31 * result + (syscrCount xor (syscrCount ushr 32)).toInt()
    result = 31 * result + (syscwCount xor (syscwCount ushr 32)).toInt()
    result = 31 * result + (readBytes xor (readBytes ushr 32)).toInt()
    result = 31 * result + (writeBytes xor (writeBytes ushr 32)).toInt()
    result = 31 * result + (cancelledWriteBytes xor (cancelledWriteBytes ushr 32)).toInt()
    result = 31 * result + (majorFaults xor (majorFaults ushr 32)).toInt()
    result = 31 * result + (blkIoTicks xor (blkIoTicks ushr 32)).toInt()

    return result
  }

  override fun toString(): String =
      "DiskMetrics{rcharBytes=${rcharBytes}, wcharBytes=${wcharBytes}, syscrCount=${syscrCount}, syscwCount=${syscwCount}, readBytes=${readBytes}, writeBytes=${writeBytes}, cancelledWriteBytes=${cancelledWriteBytes}, majorFaults=${majorFaults}, blkIoTicks=${blkIoTicks}}"
}
