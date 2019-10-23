/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.network;

import android.net.TrafficStats;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TrafficStats.class)
public class ShadowTrafficStats {

  private static long sRxBytes;
  private static long sTxBytes;

  public static void setUidRxBytes(long bytes) {
    sRxBytes = bytes;
  }

  public static void setUidTxBytes(long bytes) {
    sTxBytes = bytes;
  }

  @Implementation
  public static long getUidRxBytes(int uid) {
    return sRxBytes;
  }

  @Implementation
  public static long getUidTxBytes(int uid) {
    return sTxBytes;
  }
}
