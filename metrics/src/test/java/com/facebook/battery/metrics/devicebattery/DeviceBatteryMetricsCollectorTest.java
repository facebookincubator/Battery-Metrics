/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.devicebattery;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import com.facebook.battery.metrics.core.ShadowSystemClock;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(
    sdk = 33,
    shadows = {ShadowSystemClock.class})
public class DeviceBatteryMetricsCollectorTest {

  @Rule public final ExpectedException mExpectedException = ExpectedException.none();

  private Context mContext;

  @Before
  public void setUp() {
    mContext = mock(Context.class);
  }

  @Test
  public void testInitialSnapshot() {
    ShadowSystemClock.setElapsedRealtime(5000);
    when(mContext.registerReceiver(
            ArgumentMatchers.isNull(BroadcastReceiver.class),
            ArgumentMatchers.any(IntentFilter.class)))
        .thenReturn(createBatteryIntent(BatteryManager.BATTERY_STATUS_CHARGING, 20, 100));
    DeviceBatteryMetrics metrics = new DeviceBatteryMetrics();
    DeviceBatteryMetricsCollector collector = new DeviceBatteryMetricsCollector(mContext);
    ShadowSystemClock.setElapsedRealtime(10000);
    collector.getSnapshot(metrics);
    verifySnapshot(metrics, 20, 0, 5000);
  }

  /** Sanity check that no NPEs are thrown if the battery intent is null. */
  @Test
  public void testEmptyBatteryIntent() {
    DeviceBatteryMetrics metrics = new DeviceBatteryMetrics();
    DeviceBatteryMetricsCollector collector = new DeviceBatteryMetricsCollector(mContext);
    collector.getSnapshot(metrics);
  }

  @Test
  public void testNullSnapshot() {
    when(mContext.registerReceiver(
            ArgumentMatchers.isNull(BroadcastReceiver.class),
            ArgumentMatchers.any(IntentFilter.class)))
        .thenReturn(createBatteryIntent(BatteryManager.BATTERY_STATUS_CHARGING, 20, 100));
    DeviceBatteryMetricsCollector collector = new DeviceBatteryMetricsCollector(mContext);
    mExpectedException.expect(IllegalArgumentException.class);
    collector.getSnapshot(null);
  }

  @Test
  public void testExceptionInRegisterReceiver() {
    when(mContext.registerReceiver(
            ArgumentMatchers.isNull(BroadcastReceiver.class),
            ArgumentMatchers.any(IntentFilter.class)))
        .thenThrow(new SecurityException("Testing exception"));
    DeviceBatteryMetrics metrics = new DeviceBatteryMetrics();
    DeviceBatteryMetricsCollector collector = new DeviceBatteryMetricsCollector(mContext);
    collector.getSnapshot(metrics);
    assertThat(metrics.batteryLevelPct).isEqualTo(DeviceBatteryMetricsCollector.UNKNOWN_LEVEL);
  }

  @Test
  public void testSnapshotAfterValidBroadcasts() {
    // Set up the collector
    ShadowSystemClock.setElapsedRealtime(5000);
    when(mContext.registerReceiver(
            ArgumentMatchers.isNull(BroadcastReceiver.class),
            ArgumentMatchers.any(IntentFilter.class)))
        .thenReturn(createBatteryIntent(BatteryManager.BATTERY_STATUS_CHARGING, 50, 100));
    when(mContext.registerReceiver(
            ArgumentMatchers.isNotNull(BroadcastReceiver.class),
            ArgumentMatchers.any(IntentFilter.class)))
        .thenReturn(null);
    DeviceBatteryMetrics metrics = new DeviceBatteryMetrics();
    DeviceBatteryMetricsCollector collector = new DeviceBatteryMetricsCollector(mContext);

    // Get a handle of the receiver
    ArgumentCaptor<BroadcastReceiver> captor = ArgumentCaptor.forClass(BroadcastReceiver.class);
    verify(mContext, times(2))
        .registerReceiver(captor.capture(), ArgumentMatchers.any(IntentFilter.class));
    final List<BroadcastReceiver> receivers = captor.getAllValues();
    assertThat(receivers.size()).isEqualTo(2);
    BroadcastReceiver receiver = receivers.get(1);
    assertThat(receiver).isNotNull();

    // Simulate connecting and disconnecting power
    ShadowSystemClock.setElapsedRealtime(9000);
    receiver.onReceive(mContext, new Intent(Intent.ACTION_POWER_DISCONNECTED));
    ShadowSystemClock.setElapsedRealtime(15000);
    receiver.onReceive(mContext, new Intent(Intent.ACTION_POWER_CONNECTED));
    ShadowSystemClock.setElapsedRealtime(22000);
    receiver.onReceive(mContext, new Intent(Intent.ACTION_POWER_DISCONNECTED));

    // Get snapshot
    ShadowSystemClock.setElapsedRealtime(28000);
    when(mContext.registerReceiver(
            ArgumentMatchers.isNull(BroadcastReceiver.class),
            ArgumentMatchers.any(IntentFilter.class)))
        .thenReturn(createBatteryIntent(BatteryManager.BATTERY_STATUS_CHARGING, 20, 100));
    collector.getSnapshot(metrics);
    verifySnapshot(metrics, 20, 12000, 11000);

    // Power Connected and test getSnapshot
    ShadowSystemClock.setElapsedRealtime(30000);
    receiver.onReceive(mContext, new Intent(Intent.ACTION_POWER_CONNECTED));
    ShadowSystemClock.setElapsedRealtime(42000);
    when(mContext.registerReceiver(
            ArgumentMatchers.isNull(BroadcastReceiver.class),
            ArgumentMatchers.any(IntentFilter.class)))
        .thenReturn(createBatteryIntent(BatteryManager.BATTERY_STATUS_CHARGING, 30, 100));
    collector.getSnapshot(metrics);
    verifySnapshot(metrics, 30, 14000, 23000);

    // PowerConnected and testGetSnapshot (Check for two consecutive CONNECTED intents)
    ShadowSystemClock.setElapsedRealtime(50000);
    receiver.onReceive(mContext, new Intent(Intent.ACTION_POWER_CONNECTED));
    ShadowSystemClock.setElapsedRealtime(51000);
    when(mContext.registerReceiver(
            ArgumentMatchers.isNull(BroadcastReceiver.class),
            ArgumentMatchers.any(IntentFilter.class)))
        .thenReturn(createBatteryIntent(BatteryManager.BATTERY_STATUS_CHARGING, 40, 100));
    collector.getSnapshot(metrics);
    verifySnapshot(metrics, 40, 14000, 32000);

    // Test for 2 consecutive powerdisconnected intents
    ShadowSystemClock.setElapsedRealtime(55000);
    receiver.onReceive(mContext, new Intent(Intent.ACTION_POWER_DISCONNECTED));
    ShadowSystemClock.setElapsedRealtime(59000);
    receiver.onReceive(mContext, new Intent(Intent.ACTION_POWER_DISCONNECTED));
    ShadowSystemClock.setElapsedRealtime(65000);
    when(mContext.registerReceiver(
            ArgumentMatchers.isNull(BroadcastReceiver.class),
            ArgumentMatchers.any(IntentFilter.class)))
        .thenReturn(createBatteryIntent(BatteryManager.BATTERY_STATUS_CHARGING, 60, 100));
    collector.getSnapshot(metrics);
    verifySnapshot(metrics, 60, 24000, 36000);
  }

  private static Intent createBatteryIntent(int status, int level, int scale) {
    Intent intent = new Intent();
    intent.putExtra(BatteryManager.EXTRA_STATUS, status);
    intent.putExtra(BatteryManager.EXTRA_LEVEL, level);
    intent.putExtra(BatteryManager.EXTRA_SCALE, scale);
    return intent;
  }

  private static void verifySnapshot(
      DeviceBatteryMetrics metrics, int batteryLevel, int batteryTime, int chargingTime) {
    assertThat((int) metrics.batteryLevelPct).isEqualTo(batteryLevel);
    assertThat(metrics.batteryRealtimeMs).isEqualTo(batteryTime);
    assertThat(metrics.chargingRealtimeMs).isEqualTo(chargingTime);
  }
}
