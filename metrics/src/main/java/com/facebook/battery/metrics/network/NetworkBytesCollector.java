/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Size;

abstract public class NetworkBytesCollector {

  public static final int RX = 0b000;
  public static final int TX = 0b001;
  public static final int MOBILE = 0b010;
  public static final int WIFI = 0b000;
  public static final int FG = 0b000;
  public static final int BG = 0b100;

  public static @Size(8) long[] createByteArray() {
    return new long[8];
  }

  public abstract boolean supportsBgDistinction();

  public abstract boolean getTotalBytes(@Size(8) long[] bytes);

  @SuppressLint("ObsoleteSdkInt")
  public static NetworkBytesCollector create(Context context) {
    if (Build.VERSION.SDK_INT >= 14) {
      long[] bytes = new long[8];
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
