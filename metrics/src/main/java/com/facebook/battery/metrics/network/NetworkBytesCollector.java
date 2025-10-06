/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import androidx.annotation.Size;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
abstract class NetworkBytesCollector {

  static final int RX = 0b000;
  static final int TX = 0b001;
  static final int MOBILE = 0b010;
  static final int WIFI = 0b000;
  static final int FG = 0b000;
  static final int BG = 0b100;

  public static @Size(8) long[] createByteArray() {
    return new long[8];
  }

  public abstract boolean supportsBgDistinction();

  public abstract boolean getTotalBytes(@Size(8) long[] bytes);

  @SuppressLint("ObsoleteSdkInt")
  public static NetworkBytesCollector create(Context context) {
    if (Build.VERSION.SDK_INT >= 28) {
      return new NetworkStatsManagerBytesCollector(context);
    } else {
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
