/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;

/**
 * Collector that allows to obtain info about Radio up time by network type(MOBILE and WIFI).
 *
 * <p>When sending or receiving data, the {@link RadioStateCollector} should be then notified to
 * ensure Radio state estimations are computed correctly.
 *
 * <p>Depending on the type of Active Network the Collector will attempt to estimate when Radio
 * turns On and Off taking care of eventual tail time. The latter is the amount of time Radio might
 * be kept active to preserve battery consumption and reduce latency of transition.
 */
@ThreadSafe
public class RadioStateCollector extends SystemMetricsCollector<RadioStateMetrics> {

  private static final int NONE = -1;
  private static final int WAKEUP_INTERVAL_S = 10;

  private final ConnectivityManager mConnectivityManager;

  private final MonotonicRadioMonitor mWifiRadioMonitor = new MonotonicRadioMonitor(0);
  private final MonotonicRadioMonitor mMobileRadioMonitor =
      new MonotonicRadioMonitor(WAKEUP_INTERVAL_S);

  public RadioStateCollector(Context context) {
    mConnectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  private static int getNetworkType(NetworkInfo info) {
    return info == null ? NONE : info.getType();
  }

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(RadioStateMetrics snapshot) {

    final long mobileNextIdleTimeActive = mMobileRadioMonitor.mNextIdleTimeActive.get();
    snapshot.mobileHighPowerActiveS = MonotonicRadioMonitor.totalTxS(mobileNextIdleTimeActive);
    snapshot.mobileLowPowerActiveS = MonotonicRadioMonitor.totalTailS(mobileNextIdleTimeActive);
    snapshot.mobileRadioWakeupCount = mMobileRadioMonitor.mWakeupCounter.get();

    final long wifiNextIdleTimeActive = mWifiRadioMonitor.mNextIdleTimeActive.get();
    snapshot.wifiActiveS =
        MonotonicRadioMonitor.totalTxS(wifiNextIdleTimeActive)
            + MonotonicRadioMonitor.totalTailS(wifiNextIdleTimeActive);
    snapshot.wifiRadioWakeupCount = mWifiRadioMonitor.mWakeupCounter.get();

    return true;
  }

  @Override
  public RadioStateMetrics createMetrics() {
    return new RadioStateMetrics();
  }

  @Nullable
  private MonotonicRadioMonitor getCurrentRadioMonitor() {
    final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
    final int currentNetworkType = getNetworkType(info);
    switch (currentNetworkType) {
      case NONE:
        return null;
      case ConnectivityManager.TYPE_WIFI:
        return mWifiRadioMonitor;
      default:
        return mMobileRadioMonitor;
    }
  }

  public void onRadioActiveNow() {
    final MonotonicRadioMonitor radioMonitor = getCurrentRadioMonitor();
    if (radioMonitor != null) {
      final long timeMs = SystemClock.elapsedRealtime();
      radioMonitor.onRadioActivate(timeMs, timeMs);
    }
  }

  public void onRadioActive(long startTimeMs, long endTimeMs) {
    final MonotonicRadioMonitor radioMonitor = getCurrentRadioMonitor();
    if (radioMonitor != null) {
      radioMonitor.onRadioActivate(startTimeMs, endTimeMs);
    }
  }
}
