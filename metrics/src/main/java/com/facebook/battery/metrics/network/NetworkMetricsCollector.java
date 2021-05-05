/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
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
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.infer.annotation.Nullsafe;
import com.facebook.infer.annotation.ThreadSafe;
import java.util.Arrays;

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
@Nullsafe(Nullsafe.Mode.LOCAL)
@ThreadSafe
public class NetworkMetricsCollector extends SystemMetricsCollector<NetworkMetrics> {
  private static final String TAG = "NetworkMetricsCollector";

  private boolean mIsValid = true;
  private final NetworkBytesCollector mCollector;
  private final long[] mBytes;
  private final long[] mPrevBytes;

  public NetworkMetricsCollector(Context context) {
    mCollector = NetworkBytesCollector.create(context);
    mBytes = NetworkBytesCollector.createByteArray();
    mPrevBytes = NetworkBytesCollector.createByteArray();
  }

  @Override
  @ThreadSafe(enableChecks = false)
  public synchronized boolean getSnapshot(NetworkMetrics snapshot) {
    // Once the value has decreased, the underlying value has almost certainly reset and all current
    // snapshots are invalidated. Disable this collector.
    if (!mIsValid || !mCollector.getTotalBytes(mBytes)) {
      return false;
    }

    mIsValid = ensureBytesIncreased(mBytes, mPrevBytes);
    if (!mIsValid) {
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

  static boolean ensureBytesIncreased(long[] currentBytes, long[] previousBytes) {
    for (int i = 0; i < currentBytes.length; i++) {
      if (currentBytes[i] < previousBytes[i]) {
        SystemMetricsLogger.wtf(
            TAG,
            "Network Bytes decreased from "
                + Arrays.toString(previousBytes)
                + " to "
                + Arrays.toString(currentBytes));
        return false;
      }
    }
    System.arraycopy(currentBytes, 0, previousBytes, 0, currentBytes.length);
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
