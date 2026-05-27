/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.bluetooth

import com.facebook.battery.metrics.core.SystemMetrics

class BluetoothMetrics : SystemMetrics<BluetoothMetrics?>() {

  @JvmField var bleScanCount: Int = 0

  @JvmField var bleScanDurationMs: Long = 0

  @JvmField var bleOpportunisticScanCount: Int = 0

  @JvmField var bleOpportunisticScanDurationMs: Long = 0

  override fun sum(p0: BluetoothMetrics?, p1: BluetoothMetrics?): BluetoothMetrics {
    var output = p1
    if (output == null) {
      output = BluetoothMetrics()
    }
    if (p0 == null) {
      output.set(this)
    } else {
      output.bleScanCount = bleScanCount + p0.bleScanCount
      output.bleScanDurationMs = bleScanDurationMs + p0.bleScanDurationMs
      output.bleOpportunisticScanCount = bleOpportunisticScanCount + p0.bleOpportunisticScanCount
      output.bleOpportunisticScanDurationMs =
          bleOpportunisticScanDurationMs + p0.bleOpportunisticScanDurationMs
    }
    return output
  }

  override fun diff(p0: BluetoothMetrics?, p1: BluetoothMetrics?): BluetoothMetrics {
    var output = p1
    if (output == null) {
      output = BluetoothMetrics()
    }
    if (p0 == null) {
      output.set(this)
    } else {
      output.bleScanCount = bleScanCount - p0.bleScanCount
      output.bleScanDurationMs = bleScanDurationMs - p0.bleScanDurationMs
      output.bleOpportunisticScanCount = bleOpportunisticScanCount - p0.bleOpportunisticScanCount
      output.bleOpportunisticScanDurationMs =
          bleOpportunisticScanDurationMs - p0.bleOpportunisticScanDurationMs
    }
    return output
  }

  override fun set(p0: BluetoothMetrics): BluetoothMetrics {
    this.bleScanCount = p0.bleScanCount
    this.bleScanDurationMs = p0.bleScanDurationMs
    this.bleOpportunisticScanCount = p0.bleOpportunisticScanCount
    this.bleOpportunisticScanDurationMs = p0.bleOpportunisticScanDurationMs
    return this
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null || javaClass != other.javaClass) {
      return false
    }
    val that = other as BluetoothMetrics
    if (
        bleScanCount != that.bleScanCount ||
            bleScanDurationMs != that.bleScanDurationMs ||
            bleOpportunisticScanCount != that.bleOpportunisticScanCount ||
            bleOpportunisticScanDurationMs != that.bleOpportunisticScanDurationMs
    ) {
      return false
    }
    return true
  }

  override fun hashCode(): Int {
    var result = bleScanCount
    result = 31 * result + (bleScanDurationMs xor (bleScanDurationMs ushr 32)).toInt()
    result = 31 * result + bleOpportunisticScanCount
    result =
        (31 * result +
            (bleOpportunisticScanDurationMs xor (bleOpportunisticScanDurationMs ushr 32)).toInt())
    return result
  }

  override fun toString(): String =
      "BluetoothMetrics{bleScanCount=${bleScanCount}, bleScanDurationMs=${bleScanDurationMs}, bleOpportunisticScanCount=${bleOpportunisticScanCount}, bleOpportunisticScanDurationMs=${bleOpportunisticScanDurationMs}${'}'}"
}
