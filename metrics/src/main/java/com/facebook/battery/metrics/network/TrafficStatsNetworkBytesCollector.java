// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import static android.net.ConnectivityManager.TYPE_WIFI;
import static com.facebook.battery.metrics.network.NetworkMetricsCollector.MOBILE;
import static com.facebook.battery.metrics.network.NetworkMetricsCollector.RX;
import static com.facebook.battery.metrics.network.NetworkMetricsCollector.TX;
import static com.facebook.battery.metrics.network.NetworkMetricsCollector.WIFI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.support.annotation.VisibleForTesting;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * TrafficStats doesn't allow distinguishing bytes by network type in old versions of Android:
 * here we rely on attributing bytes transferred to the currently active network
 *
 * The initial bytes are arbitrarily mapped to mobile initially, because they'll get replaced
 * anyways. For Types of network that aren't understood (they vary across android versions, etc.)
 * explicitly default to Mobile similar to the approach in {@code QTagUidBytesCollector}.
 *
 * TODO(#16381416): Accept a background handler for broadcasts
 */
@NotThreadSafe
class TrafficStatsNetworkBytesCollector extends NetworkBytesCollector {

  private static final int UID = android.os.Process.myUid();
  private static final int TYPE_NONE = -1;

  private final ConnectivityManager mConnectivityManager;
  private final long[] mTotalBytes = new long[4];
  private int mCurrentNetworkType;
  private boolean mIsValid = true;

  @VisibleForTesting
  BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
      int type;
      if (info == null || (type = info.getType()) == mCurrentNetworkType) {
        return;
      }

      updateTotalBytes();
      mCurrentNetworkType = type;
    }
  };

  public TrafficStatsNetworkBytesCollector(Context context) {
    context = context.getApplicationContext();
    mConnectivityManager = (ConnectivityManager)
        context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
    mCurrentNetworkType = activeNetwork == null ? TYPE_NONE : activeNetwork.getType();
    context.registerReceiver(
        mReceiver,
        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    updateTotalBytes();
  }

  @Override
  public synchronized boolean getTotalBytes(long[] bytes) {
    if (!mIsValid) {
      return false;
    }

    updateTotalBytes();
    System.arraycopy(mTotalBytes, 0, bytes, 0, bytes.length);
    return true;
  }

  private synchronized void updateTotalBytes() {
    long currentTotalTxBytes = TrafficStats.getUidTxBytes(UID);
    long currentTotalRxBytes = TrafficStats.getUidRxBytes(UID);

    if (currentTotalRxBytes == TrafficStats.UNSUPPORTED ||
        currentTotalTxBytes == TrafficStats.UNSUPPORTED) {
      mIsValid = false;
      return;
    }

    int prefix = mCurrentNetworkType == TYPE_WIFI ? WIFI : MOBILE;

    long lastTotalTxBytes = mTotalBytes[TX | MOBILE] + mTotalBytes[TX | WIFI];
    long lastTotalRxBytes = mTotalBytes[RX | MOBILE] + mTotalBytes[RX | WIFI];

    mTotalBytes[TX | prefix] += currentTotalTxBytes - lastTotalTxBytes;
    mTotalBytes[RX | prefix] += currentTotalRxBytes - lastTotalRxBytes;
  }
}
