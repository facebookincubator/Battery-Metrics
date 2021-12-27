/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.bluetooth;

import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;

public class BluetoothMetrics extends SystemMetrics<BluetoothMetrics> {

  public int bleScanCount;
  public long bleScanDurationMs;
  public int bleOpportunisticScanCount;
  public long bleOpportunisticScanDurationMs;

  @Override
  public BluetoothMetrics sum(@Nullable BluetoothMetrics b, @Nullable BluetoothMetrics output) {
    if (output == null) {
      output = new BluetoothMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      output.bleScanCount = bleScanCount + b.bleScanCount;
      output.bleScanDurationMs = bleScanDurationMs + b.bleScanDurationMs;
      output.bleOpportunisticScanCount = bleOpportunisticScanCount + b.bleOpportunisticScanCount;
      output.bleOpportunisticScanDurationMs =
          bleOpportunisticScanDurationMs + b.bleOpportunisticScanDurationMs;
    }
    return output;
  }

  @Override
  public BluetoothMetrics diff(@Nullable BluetoothMetrics b, @Nullable BluetoothMetrics output) {
    if (output == null) {
      output = new BluetoothMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      output.bleScanCount = bleScanCount - b.bleScanCount;
      output.bleScanDurationMs = bleScanDurationMs - b.bleScanDurationMs;
      output.bleOpportunisticScanCount = bleOpportunisticScanCount - b.bleOpportunisticScanCount;
      output.bleOpportunisticScanDurationMs =
          bleOpportunisticScanDurationMs - b.bleOpportunisticScanDurationMs;
    }
    return output;
  }

  @Override
  public BluetoothMetrics set(BluetoothMetrics b) {
    bleScanCount = b.bleScanCount;
    bleScanDurationMs = b.bleScanDurationMs;
    bleOpportunisticScanCount = b.bleOpportunisticScanCount;
    bleOpportunisticScanDurationMs = b.bleOpportunisticScanDurationMs;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BluetoothMetrics that = (BluetoothMetrics) o;
    if (bleScanCount != that.bleScanCount
        || bleScanDurationMs != that.bleScanDurationMs
        || bleOpportunisticScanCount != that.bleOpportunisticScanCount
        || bleOpportunisticScanDurationMs != that.bleOpportunisticScanDurationMs) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = bleScanCount;
    result = 31 * result + (int) (bleScanDurationMs ^ (bleScanDurationMs >>> 32));
    result = 31 * result + bleOpportunisticScanCount;
    result =
        31 * result
            + (int) (bleOpportunisticScanDurationMs ^ (bleOpportunisticScanDurationMs >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "BluetoothMetrics{"
        + "bleScanCount="
        + bleScanCount
        + ", bleScanDurationMs="
        + bleScanDurationMs
        + ", bleOpportunisticScanCount="
        + bleOpportunisticScanCount
        + ", bleOpportunisticScanDurationMs="
        + bleOpportunisticScanDurationMs
        + '}';
  }
}
