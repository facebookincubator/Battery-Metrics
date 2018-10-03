// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
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
    } catch (RemoteException re) {
      SystemMetricsLogger.wtf(TAG, "Unable to get bytes transferred", re);
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
