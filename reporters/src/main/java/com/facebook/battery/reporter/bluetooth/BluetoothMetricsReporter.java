/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.bluetooth;

import com.facebook.battery.metrics.bluetooth.BluetoothMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class BluetoothMetricsReporter implements SystemMetricsReporter<BluetoothMetrics> {

  public static final String BLE_SCAN_COUNT = "ble_scan_count";
  public static final String BLE_SCAN_DURATION_MS = "ble_scan_duration_ms";
  public static final String BLE_OPPORTUNISTIC_SCAN_COUNT = "ble_opportunistic_scan_count";
  public static final String BLE_OPPORTUNISTIC_SCAN_DURATION_MS =
      "ble_opportunistic_scan_duration_ms";

  @Override
  public void reportTo(BluetoothMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.bleScanCount != 0) {
      event.add(BLE_SCAN_COUNT, metrics.bleScanCount);
    }

    if (metrics.bleScanDurationMs != 0) {
      event.add(BLE_SCAN_DURATION_MS, metrics.bleScanDurationMs);
    }

    if (metrics.bleOpportunisticScanCount != 0) {
      event.add(BLE_OPPORTUNISTIC_SCAN_COUNT, metrics.bleOpportunisticScanCount);
    }

    if (metrics.bleScanDurationMs != 0) {
      event.add(BLE_OPPORTUNISTIC_SCAN_DURATION_MS, metrics.bleOpportunisticScanDurationMs);
    }
  }
}
