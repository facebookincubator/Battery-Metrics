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

  override fun sum(b: BluetoothMetrics?, output: BluetoothMetrics?): BluetoothMetrics {
    var output = output
    if (output == null) {
      output = BluetoothMetrics()
    }
    if (b == null) {
      output.set(this)
    } else {
      output.bleScanCount = bleScanCount + b.bleScanCount
      output.bleScanDurationMs = bleScanDurationMs + b.bleScanDurationMs
      output.bleOpportunisticScanCount = bleOpportunisticScanCount + b.bleOpportunisticScanCount
      output.bleOpportunisticScanDurationMs =
          bleOpportunisticScanDurationMs + b.bleOpportunisticScanDurationMs
    }
    return output
  }

  override fun diff(b: BluetoothMetrics?, output: BluetoothMetrics?): BluetoothMetrics {
    var output = output
    if (output == null) {
      output = BluetoothMetrics()
    }
    if (b == null) {
      output.set(this)
    } else {
      output.bleScanCount = bleScanCount - b.bleScanCount
      output.bleScanDurationMs = bleScanDurationMs - b.bleScanDurationMs
      output.bleOpportunisticScanCount = bleOpportunisticScanCount - b.bleOpportunisticScanCount
      output.bleOpportunisticScanDurationMs =
          bleOpportunisticScanDurationMs - b.bleOpportunisticScanDurationMs
    }
    return output
  }

  override fun set(b: BluetoothMetrics): BluetoothMetrics {
    this.bleScanCount = b.bleScanCount
    this.bleScanDurationMs = b.bleScanDurationMs
    this.bleOpportunisticScanCount = b.bleOpportunisticScanCount
    this.bleOpportunisticScanDurationMs = b.bleOpportunisticScanDurationMs
    return this
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) {
      return true
    }
    if (o == null || javaClass != o.javaClass) {
      return false
    }
    val that = o as BluetoothMetrics
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
