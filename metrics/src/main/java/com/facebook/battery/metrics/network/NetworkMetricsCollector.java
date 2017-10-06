// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import android.content.Context;
import com.facebook.battery.metrics.api.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;

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
