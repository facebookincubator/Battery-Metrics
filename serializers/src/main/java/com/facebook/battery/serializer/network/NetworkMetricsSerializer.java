/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.network;

import com.facebook.battery.metrics.network.NetworkMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.infer.annotation.Nullsafe;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class NetworkMetricsSerializer extends SystemMetricsSerializer<NetworkMetrics> {

  private static final long serialVersionUID = -2479634339626480691L;

  @Override
  public long getTag() {
    return serialVersionUID;
  }

  @Override
  public void serializeContents(NetworkMetrics metrics, DataOutput output) throws IOException {
    output.writeLong(metrics.mobileBytesRx);
    output.writeLong(metrics.mobileBytesTx);
    output.writeLong(metrics.wifiBytesRx);
    output.writeLong(metrics.wifiBytesTx);
  }

  @Override
  public boolean deserializeContents(NetworkMetrics metrics, DataInput input) throws IOException {
    metrics.mobileBytesRx = input.readLong();
    metrics.mobileBytesTx = input.readLong();
    metrics.wifiBytesRx = input.readLong();
    metrics.wifiBytesTx = input.readLong();
    return true;
  }
}
