/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.network;

import android.content.Context;
import com.facebook.battery.metrics.api.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;

/**
 * Records data transferred by the current application, broken down by type of network (radio vs
 * wifi) and bytes received and transmitted.
 *
 * <p>This tries to use a qtaguid file if available, but otherwise falls back to TrafficStats with
 * manual instrumentation for guessing which type of network is active.
 */
@ThreadSafe
public class NetworkMetricsCollector extends SystemMetricsCollector<NetworkMetrics> {

  static final int RX = 0b00;
  static final int TX = 0b01;
  static final int MOBILE = 0b10;
  static final int WIFI = 0b00;

  private final NetworkBytesCollector mNetworkBytesCollector;
  private final long[] mBytes = new long[4];

  public NetworkMetricsCollector(Context context) {
    mNetworkBytesCollector = NetworkBytesCollector.create(context);
  }

  @Override
  @ThreadSafe(enableChecks = false)
  public synchronized boolean getSnapshot(NetworkMetrics snapshot) {
    if (!mNetworkBytesCollector.getTotalBytes(mBytes)) {
      return false;
    }

    snapshot.mobileBytesTx = mBytes[MOBILE | TX];
    snapshot.mobileBytesRx = mBytes[MOBILE | RX];
    snapshot.wifiBytesTx = mBytes[WIFI | TX];
    snapshot.wifiBytesRx = mBytes[WIFI | RX];
    return true;
  }

  @Override
  public NetworkMetrics createMetrics() {
    return new NetworkMetrics();
  }
}
