/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.network;

import static com.facebook.battery.metrics.network.NetworkBytesCollector.BG;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.FG;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.MOBILE;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.RX;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.TX;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.WIFI;

import android.content.Context;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;

/**
 * Records data transferred by the current application, broken down by type of network (radio vs
 * wifi) and bytes received and transmitted.
 *
 * <p>This tries to use a qtaguid file if available, but otherwise falls back to TrafficStats with
 * manual instrumentation for guessing which type of network is active.
 *
 * <p>See {@link EnhancedNetworkMetricsCollector} for distinguishing between foreground/background
 * app states as well.
 */
@ThreadSafe
public class NetworkMetricsCollector extends SystemMetricsCollector<NetworkMetrics> {

  private final NetworkBytesCollector mCollector;
  private final long[] mBytes;

  public NetworkMetricsCollector(Context context) {
    mCollector = NetworkBytesCollector.create(context);
    mBytes = NetworkBytesCollector.createByteArray();
  }

  @Override
  @ThreadSafe(enableChecks = false)
  public synchronized boolean getSnapshot(NetworkMetrics snapshot) {
    if (!mCollector.getTotalBytes(mBytes)) {
      return false;
    }

    boolean supportsBgDetection = mCollector.supportsBgDistinction();
    resetMetrics(snapshot);
    addMetricsFromBytes(snapshot, mBytes, FG);
    if (supportsBgDetection) {
      addMetricsFromBytes(snapshot, mBytes, BG);
    }
    return true;
  }

  static void addMetricsFromBytes(NetworkMetrics metrics, long[] bytes, int fgBgBit) {
    metrics.mobileBytesTx += bytes[MOBILE | TX | fgBgBit];
    metrics.mobileBytesRx += bytes[MOBILE | RX | fgBgBit];
    metrics.wifiBytesTx += bytes[WIFI | TX | fgBgBit];
    metrics.wifiBytesRx += bytes[WIFI | RX | fgBgBit];
  }

  static void resetMetrics(NetworkMetrics metrics) {
    metrics.mobileBytesTx = 0;
    metrics.mobileBytesRx = 0;
    metrics.wifiBytesTx = 0;
    metrics.wifiBytesRx = 0;
  }

  @Override
  public NetworkMetrics createMetrics() {
    return new NetworkMetrics();
  }
}
