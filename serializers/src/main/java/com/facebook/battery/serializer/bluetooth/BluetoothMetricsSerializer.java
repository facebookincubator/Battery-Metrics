/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.serializer.bluetooth;

import com.facebook.battery.metrics.bluetooth.BluetoothMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BluetoothMetricsSerializer extends SystemMetricsSerializer<BluetoothMetrics> {

  private static final long serialVersionUID = -4085774432413599882L;

  @Override
  public long getTag() {
    return serialVersionUID;
  }

  @Override
  public void serializeContents(BluetoothMetrics metrics, DataOutput output) throws IOException {
    output.writeInt(metrics.bleScanCount);
    output.writeLong(metrics.bleScanDurationMs);
    output.writeInt(metrics.bleOpportunisticScanCount);
    output.writeLong(metrics.bleOpportunisticScanDurationMs);
  }

  @Override
  public boolean deserializeContents(BluetoothMetrics metrics, DataInput input) throws IOException {
    metrics.bleScanCount = input.readInt();
    metrics.bleScanDurationMs = input.readLong();
    metrics.bleOpportunisticScanCount = input.readInt();
    metrics.bleOpportunisticScanDurationMs = input.readLong();
    return true;
  }
}
