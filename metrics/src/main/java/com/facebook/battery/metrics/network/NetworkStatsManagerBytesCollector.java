/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.network;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import androidx.annotation.RequiresApi;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import java.util.Arrays;

@RequiresApi(api = 28)
public class NetworkStatsManagerBytesCollector extends NetworkBytesCollector {

  private static final String TAG = "NetworkStatsManagerBytesCollector";
  private final NetworkStats.Bucket mBucket = new NetworkStats.Bucket();
  private final NetworkStatsManager mNetworkStatsManager;
  private final long mStartTime;

  public NetworkStatsManagerBytesCollector(Context context) {
    mNetworkStatsManager = context.getSystemService(NetworkStatsManager.class);
    mStartTime = Long.MIN_VALUE;
  }

  @Override
  public boolean supportsBgDistinction() {
    return true;
  }

  @Override
  public boolean getTotalBytes(long[] bytes) {
    long endTimeMs = Long.MAX_VALUE;

    try {
      Arrays.fill(bytes, 0);
      getBytesForType(bytes, ConnectivityManager.TYPE_MOBILE, MOBILE, mStartTime, endTimeMs);
      getBytesForType(bytes, ConnectivityManager.TYPE_WIFI, WIFI, mStartTime, endTimeMs);
      return true;
    } catch (NullPointerException | RemoteException ex) {
      // Simply catching and failing silently, just like Android does.
      // http://androidxref.com/9.0.0_r3/xref/frameworks/base/services/core/java/com/android/server/net/NetworkStatsService.java#627
      SystemMetricsLogger.wtf(TAG, "Unable to get bytes transferred", ex);
      return false;
    }
  }

  private void getBytesForType(
      long[] bytes, int connectivityManagerType, int type, long startTimeMs, long endTimeMs)
      throws RemoteException {
    NetworkStats stats =
        mNetworkStatsManager.querySummary(connectivityManagerType, null, startTimeMs, endTimeMs);
    while (stats.hasNextBucket()) {
      stats.getNextBucket(mBucket);

      int appState =
          mBucket.getState() == NetworkStats.Bucket.STATE_FOREGROUND
              ? NetworkBytesCollector.FG
              : NetworkBytesCollector.BG;
      bytes[type | RX | appState] += mBucket.getRxBytes();
      bytes[type | TX | appState] += mBucket.getTxBytes();
    }
    stats.close();
  }
}
