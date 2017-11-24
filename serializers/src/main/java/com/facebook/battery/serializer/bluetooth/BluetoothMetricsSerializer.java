/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.bluetooth;

import com.facebook.battery.metrics.bluetooth.BluetoothMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BluetoothMetricsSerializer extends SystemMetricsSerializer<BluetoothMetrics> {

  @Override
  public void serializeContents(BluetoothMetrics metrics, DataOutput output) throws IOException {
    output.writeInt(metrics.bleScanCount);
    output.writeLong(metrics.bleScanDurationMs);
  }

  @Override
  public boolean deserializeContents(BluetoothMetrics metrics, DataInput input) throws IOException {
    metrics.bleScanCount = input.readInt();
    metrics.bleScanDurationMs = input.readLong();
    return true;
  }
}
