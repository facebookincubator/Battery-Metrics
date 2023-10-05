/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.network;

import static android.net.ConnectivityManager.TYPE_WIFI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.facebook.battery.metrics.core.VisibleToAvoidSynthetics;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * TrafficStats doesn't allow distinguishing bytes by network type in old versions of Android: here
 * we rely on attributing bytes transferred to the currently active network
 *
 * <p>The initial bytes are arbitrarily mapped to mobile initially, because they'll get replaced
 * anyways. For Types of network that aren't understood (they vary across android versions, etc.)
 * explicitly default to Mobile similar to the approach in {@code QTagUidBytesCollector}.
 *
 * <p>TODO(#16381416): Accept a background handler for broadcasts
 */
@NotThreadSafe
class TrafficStatsNetworkBytesCollector extends NetworkBytesCollector {

  private static final int UID = android.os.Process.myUid();
  private static final int TYPE_NONE = -1;

  @VisibleToAvoidSynthetics final ConnectivityManager mConnectivityManager;
  private final long[] mTotalBytes = new long[8];
  @VisibleToAvoidSynthetics int mCurrentNetworkType;
  private boolean mIsValid = true;

  @VisibleForTesting
  BroadcastReceiver mReceiver =
      new BroadcastReceiver() {
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
    // This can happen if the context is passed in while creating the application itself
    Context applicationContext = context.getApplicationContext();
    context = applicationContext != null ? applicationContext : context;

    mConnectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
    mCurrentNetworkType = activeNetwork == null ? TYPE_NONE : activeNetwork.getType();
    registerProtectedBroadcastReceiver(
        context, mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    updateTotalBytes();
  }

  @Override
  public boolean supportsBgDistinction() {
    return false;
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

  /**
   * Register receivers for protected broadcasts (these are broadcasts that can only be sent by the
   * system). This is to avoid attacks in case the protected broadcast action becomes non-protected
   * in the future version of Android, and to help the security team correctly analyze the risk of
   * these receivers.
   *
   * <p>https://developer.android.com/about/versions/14/behavior-changes-14#runtime-receivers-exported
   */
  private @Nullable Intent registerProtectedBroadcastReceiver(
      Context context, @Nullable BroadcastReceiver receiver, IntentFilter filter) {
    return context.registerReceiver(receiver, filter);
  }

  @VisibleToAvoidSynthetics
  synchronized void updateTotalBytes() {
    long currentTotalTxBytes = TrafficStats.getUidTxBytes(UID);
    long currentTotalRxBytes = TrafficStats.getUidRxBytes(UID);

    if (currentTotalRxBytes == TrafficStats.UNSUPPORTED
        || currentTotalTxBytes == TrafficStats.UNSUPPORTED) {
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
