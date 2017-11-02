/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.bluetooth;

import com.facebook.battery.metrics.bluetooth.BluetoothMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;

public class BluetoothMetricsReporter implements SystemMetricsReporter<BluetoothMetrics> {

  public static final String BLE_SCAN_COUNT = "ble_scan_count";
  public static final String BLE_SCAN_DURATION_MS = "ble_scan_duration_ms";

  @Override
  public void reportTo(BluetoothMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.bleScanCount != 0) {
      event.add(BLE_SCAN_COUNT, metrics.bleScanCount);
    }

    if (metrics.bleScanDurationMs != 0) {
      event.add(BLE_SCAN_DURATION_MS, metrics.bleScanDurationMs);
    }
  }
}
