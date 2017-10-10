/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.network;

import com.facebook.battery.metrics.network.NetworkMetrics;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NetworkMetricsSerializer extends SystemMetricsSerializer<NetworkMetrics> {

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
