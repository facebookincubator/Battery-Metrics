// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import static java.lang.Math.max;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import com.facebook.battery.metrics.api.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

  private final RadioMonitor mWifiRadioMonitor = new RadioMonitor(0);
  private final RadioMonitor mMobileRadioMonitor = new RadioMonitor(WAKEUP_INTERVAL_S);

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
    snapshot.mobileHighPowerActiveS = RadioMonitor.totalTxS(mobileNextIdleTimeActive);
    snapshot.mobileLowPowerActiveS = RadioMonitor.totalTailS(mobileNextIdleTimeActive);
    snapshot.mobileRadioWakeupCount = mMobileRadioMonitor.mWakeupCounter.get();

    final long wifiNextIdleTimeActive = mWifiRadioMonitor.mNextIdleTimeActive.get();
    snapshot.wifiActiveS =
        RadioMonitor.totalTxS(wifiNextIdleTimeActive)
            + RadioMonitor.totalTailS(wifiNextIdleTimeActive);
    snapshot.wifiRadioWakeupCount = mWifiRadioMonitor.mWakeupCounter.get();

    return true;
  }

  @Override
  public RadioStateMetrics createMetrics() {
    return new RadioStateMetrics();
  }

  @Nullable
  private RadioMonitor getCurrentRadioMonitor() {
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
    final RadioMonitor radioMonitor = getCurrentRadioMonitor();
    if (radioMonitor != null) {
      final long timeMs = SystemClock.elapsedRealtime();
      radioMonitor.onRadioActivate(timeMs, timeMs);
    }
  }

  public void onRadioActive(long startTimeMs, long endTimeMs) {
    final RadioMonitor radioMonitor = getCurrentRadioMonitor();
    if (radioMonitor != null) {
      radioMonitor.onRadioActivate(startTimeMs, endTimeMs);
    }
  }

  @ThreadSafe
  static final class RadioMonitor {

    private final int mWakeUpTimeS;

    // packed next second when it becomes idle (32bit),
    // total transfer seconds (16bits), and total tail seconds (16 bits) (at next idle)
    final AtomicLong mNextIdleTimeActive = new AtomicLong();
    final AtomicInteger mWakeupCounter = new AtomicInteger();

    public RadioMonitor(int wakeUpTimeS) {
      mWakeUpTimeS = wakeUpTimeS;
    }

    public int onRadioActivate(long transferStartMs, long transferEndMs) {
      long transferStartS = TimeUnit.MILLISECONDS.toSeconds(transferStartMs);
      long transferEndS = TimeUnit.MILLISECONDS.toSeconds(transferEndMs);
      long expectedNextIdleAndTotals;
      long newNextIdleAndTotals;
      boolean casSucceeded = false;

      /*
       * Theoretically, it's possible that a call to this function could be made,
       * not be scheduled for about 10 seconds, and then another call could be scheduled before it,
       * meaning that a wakeup/active time that occurred would not be counted.
       * Realistically, the odds of a thread going 10 seconds without being scheduled are fairly
       * low so it's not a huge worry.
       */

      do {
        expectedNextIdleAndTotals = mNextIdleTimeActive.get();
        newNextIdleAndTotals =
            adjustTotalsAndNextIdle(transferStartS, transferEndS, expectedNextIdleAndTotals);
      } while (nextIdle(expectedNextIdleAndTotals) < nextIdle(newNextIdleAndTotals)
          && !(casSucceeded =
              mNextIdleTimeActive.compareAndSet(expectedNextIdleAndTotals, newNextIdleAndTotals)));

      if (casSucceeded) {
        if (nextIdle(expectedNextIdleAndTotals) <= transferStartS) {
          mWakeupCounter.getAndIncrement();
        }
        return totalTxS(expectedNextIdleAndTotals) + totalTailS(expectedNextIdleAndTotals);
      } else {
        return 0;
      }
    }

    private long adjustTotalsAndNextIdle(
        long transferStartS, long transferEndS, long nextIdleAndTotals) {
      long nextIdle = nextIdle(nextIdleAndTotals);
      long oldTransferEnd = nextIdle - mWakeUpTimeS;
      // If transfer end goes into new second we want at least one second for transfer
      long transferDeltaAdjustment = transferEndS > oldTransferEnd ? 1L : 0L;
      transferStartS = max(transferStartS, oldTransferEnd);
      transferEndS = max(transferEndS, oldTransferEnd);

      long oldTransferTotal = totalTxS(nextIdleAndTotals);
      long oldTailTotal = totalTailS(nextIdleAndTotals);

      long transferDelta = max(transferEndS - transferStartS, transferDeltaAdjustment);

      long tailDelta;
      if (transferStartS < nextIdle) {
        tailDelta =
            (transferEndS < nextIdle)
                ? mWakeUpTimeS - transferDelta - (nextIdle - transferEndS)
                : mWakeUpTimeS - (nextIdle - transferStartS);
      } else {
        tailDelta = mWakeUpTimeS;
      }

      return makeIdleValue(
          transferEndS + mWakeUpTimeS, oldTransferTotal + transferDelta, oldTailTotal + tailDelta);
    }

    static long makeIdleValue(long nextIdleS, long totalTransferS, long totalTailS) {
      return (nextIdleS << 32) | (totalTransferS << 16) | totalTailS;
    }

    static long nextIdle(long nextIdleTimeActive) {
      return nextIdleTimeActive >> 32;
    }

    static int totalTxS(long nextIdleTimeActive) {
      return (int) ((nextIdleTimeActive & 0xFFFF0000L) >> 16);
    }

    static int totalTailS(long nextIdleTimeActive) {
      return (int) (nextIdleTimeActive & 0xFFFFL);
    }
  }
}
