// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import static com.facebook.battery.metrics.network.NetworkBytesCollector.BG;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.FG;

import android.content.Context;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;

/**
 * Alternative to {@link NetworkMetricsCollector} which supports distinguishing
 * foreground/background app states where possible (in particular when using qtaguid stats).
 */
public class EnhancedNetworkMetricsCollector
    extends SystemMetricsCollector<EnhancedNetworkMetrics> {

  private final NetworkBytesCollector mCollector;
  private final long[] mBytes;

  public EnhancedNetworkMetricsCollector(Context context) {
    mCollector = NetworkBytesCollector.create(context);
    mBytes = NetworkBytesCollector.createByteArray();
  }

  @Override
  @ThreadSafe(enableChecks = false)
  public synchronized boolean getSnapshot(EnhancedNetworkMetrics snapshot) {
    if (!mCollector.getTotalBytes(mBytes)) {
      return false;
    }

    boolean supportsBgDetection = mCollector.supportsBgDistinction();
    snapshot.supportsBgDetection = supportsBgDetection;
    NetworkMetricsCollector.resetMetrics(snapshot.fgMetrics);
    NetworkMetricsCollector.addMetricsFromBytes(snapshot.fgMetrics, mBytes, FG);
    if (supportsBgDetection) {
      NetworkMetricsCollector.resetMetrics(snapshot.bgMetrics);
      NetworkMetricsCollector.addMetricsFromBytes(snapshot.bgMetrics, mBytes, BG);
    }
    return true;
  }

  @Override
  public EnhancedNetworkMetrics createMetrics() {
    return new EnhancedNetworkMetrics();
  }
}
