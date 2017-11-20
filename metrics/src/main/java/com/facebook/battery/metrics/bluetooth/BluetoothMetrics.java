/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.bluetooth;

import android.support.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;

public class BluetoothMetrics extends SystemMetrics<BluetoothMetrics> {

  public int bleScanCount;
  public long bleScanDurationMs;

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
    }
    return output;
  }

  @Override
  public BluetoothMetrics set(BluetoothMetrics b) {
    bleScanCount = b.bleScanCount;
    bleScanDurationMs = b.bleScanDurationMs;
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
    if (bleScanCount != that.bleScanCount) {
      return false;
    }
    return bleScanDurationMs == that.bleScanDurationMs;
  }

  @Override
  public int hashCode() {
    int result = bleScanCount;
    result = 31 * result + (int) (bleScanDurationMs ^ (bleScanDurationMs >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "BluetoothMetrics{"
        + "bleScanCount="
        + bleScanCount
        + ", bleScanDurationMs="
        + bleScanDurationMs
        + '}';
  }
}
