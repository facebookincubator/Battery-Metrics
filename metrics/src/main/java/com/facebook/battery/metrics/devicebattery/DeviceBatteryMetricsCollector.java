/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.devicebattery;

import static com.facebook.battery.metrics.core.Utilities.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.core.VisibleToAvoidSynthetics;
import com.facebook.infer.annotation.ThreadSafe;
import javax.annotation.concurrent.GuardedBy;

/**
 * Collects data about {@link DeviceBatteryMetrics}. This relies on maintaining the charging state
 * and hence will probably not work in Doze mode (N+) where the broadcasts for power
 * connected/disconnected are not guranteed to be relayed to the app.
 */
@ThreadSafe
public class DeviceBatteryMetricsCollector extends SystemMetricsCollector<DeviceBatteryMetrics> {
  private static final String TAG = "DeviceBatteryMetricsCollector";
  static int UNKNOWN_LEVEL = -1;

  private final Context mContext;

  @GuardedBy("this")
  @VisibleToAvoidSynthetics
  long mBatteryRealtimeMs;

  @GuardedBy("this")
  @VisibleToAvoidSynthetics
  long mChargingRealtimeMs;

  @GuardedBy("this")
  @VisibleToAvoidSynthetics
  long mLastUpdateMs;

  @GuardedBy("this")
  @VisibleToAvoidSynthetics
  boolean mIsCurrentlyCharging;

  public DeviceBatteryMetricsCollector(Context context) {
    mContext = context;

    // Initialize the current state
    mIsCurrentlyCharging = isCharging(getBatteryIntent());
    mLastUpdateMs = SystemClock.elapsedRealtime();

    // Init the intentFilter
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
    intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

    // Register the receiver for power connected and disconnected
    context.registerReceiver(
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            long now = SystemClock.elapsedRealtime();
            synchronized (DeviceBatteryMetricsCollector.this) {
              switch (intent.getAction()) {
                case Intent.ACTION_POWER_CONNECTED:
                  if (!mIsCurrentlyCharging) {
                    mBatteryRealtimeMs += (now - mLastUpdateMs);
                  } else {
                    // This should not happen
                    mChargingRealtimeMs += (now - mLastUpdateMs);
                    logIncorrectSequence("CONNECTED", now);
                  }
                  mIsCurrentlyCharging = true;
                  break;

                case Intent.ACTION_POWER_DISCONNECTED:
                  if (mIsCurrentlyCharging) {
                    mChargingRealtimeMs += (now - mLastUpdateMs);
                  } else {
                    // This should not happen
                    mBatteryRealtimeMs += (now - mLastUpdateMs);
                    logIncorrectSequence("DISCONNECTED", now);
                  }
                  mIsCurrentlyCharging = false;
                  break;
                default:
              }
              mLastUpdateMs = now;
            }
          }
        },
        intentFilter);
  }

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(DeviceBatteryMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");
    snapshot.batteryLevelPct = getBatteryLevel(getBatteryIntent());
    long now = SystemClock.elapsedRealtime();
    synchronized (this) {
      if (mIsCurrentlyCharging) {
        snapshot.chargingRealtimeMs = mChargingRealtimeMs + (now - mLastUpdateMs);
        snapshot.batteryRealtimeMs = mBatteryRealtimeMs;
      } else {
        snapshot.chargingRealtimeMs = mChargingRealtimeMs;
        snapshot.batteryRealtimeMs = mBatteryRealtimeMs + (now - mLastUpdateMs);
      }
      return true;
    }
  }

  @Override
  public DeviceBatteryMetrics createMetrics() {
    return new DeviceBatteryMetrics();
  }

  /** This can be null for devices without any battery (like a TV) or because of buggy firmware. */
  private @Nullable Intent getBatteryIntent() {
    try {
      return mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    } catch (Exception ex) {
      // There is a weird bug that causes this to crash sometimes in android versions <= 4.2
      // with a SecurityException - handling the exception to avoid the crash
      SystemMetricsLogger.wtf(TAG, "Exception registering receiver for ACTION_BATTERY_CHANGED");
      return null;
    }
  }

  private static float getBatteryLevel(@Nullable Intent batteryStatus) {
    if (batteryStatus == null) {
      return UNKNOWN_LEVEL;
    }

    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    if (level < 0 || scale <= 0) {
      return UNKNOWN_LEVEL;
    }
    return (((float) level / scale) * 100);
  }

  private static boolean isCharging(@Nullable Intent batteryStatus) {
    int status =
        batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;
    return status == BatteryManager.BATTERY_STATUS_CHARGING
        || status == BatteryManager.BATTERY_STATUS_FULL;
  }

  /**
   * Log an error if we get two intents for POWER_CONNECTED or POWER_DISCONNECTED in a row
   *
   * @param intentType
   * @param now
   */
  @VisibleToAvoidSynthetics
  void logIncorrectSequence(String intentType, long now) {
    SystemMetricsLogger.wtf(
        TAG, "Consecutive " + intentType + "broadcasts: (" + mLastUpdateMs + ", " + now + ")");
  }
}
