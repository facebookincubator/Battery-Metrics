// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import android.content.Context;
import android.os.Build;

abstract public class NetworkBytesCollector {

  abstract boolean getTotalBytes(long[] bytes);

  public static NetworkBytesCollector create(Context context) {
    if (Build.VERSION.SDK_INT >= 14) {
      long[] bytes = new long[4];
      QTagUidNetworkBytesCollector collector = new QTagUidNetworkBytesCollector();

      // Sanity check that the collector can work before falling back to the UidStats based
      // collector.
      if (collector.getTotalBytes(bytes)) {
        return collector;
      }
    }
    return new TrafficStatsNetworkBytesCollector(context);
  }
}
