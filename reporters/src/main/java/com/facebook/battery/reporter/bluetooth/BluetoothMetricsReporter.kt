/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.bluetooth

import com.facebook.battery.metrics.bluetooth.BluetoothMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class BluetoothMetricsReporter : SystemMetricsReporter<BluetoothMetrics> {

  override fun reportTo(metrics: BluetoothMetrics, event: SystemMetricsReporter.Event) {
    if (metrics.bleScanCount != 0) {
      event.add(BLE_SCAN_COUNT, metrics.bleScanCount)
    }

    if (metrics.bleScanDurationMs != 0L) {
      event.add(BLE_SCAN_DURATION_MS, metrics.bleScanDurationMs)
    }

    if (metrics.bleOpportunisticScanCount != 0) {
      event.add(BLE_OPPORTUNISTIC_SCAN_COUNT, metrics.bleOpportunisticScanCount)
    }

    if (metrics.bleScanDurationMs != 0L) {
      event.add(BLE_OPPORTUNISTIC_SCAN_DURATION_MS, metrics.bleOpportunisticScanDurationMs)
    }
  }

  companion object {
    const val BLE_SCAN_COUNT: String = "ble_scan_count"
    const val BLE_SCAN_DURATION_MS: String = "ble_scan_duration_ms"
    const val BLE_OPPORTUNISTIC_SCAN_COUNT: String = "ble_opportunistic_scan_count"
    const val BLE_OPPORTUNISTIC_SCAN_DURATION_MS: String = "ble_opportunistic_scan_duration_ms"
  }
}
