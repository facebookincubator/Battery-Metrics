/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.bluetooth;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

import android.bluetooth.le.ScanCallback;
import com.facebook.battery.metrics.core.ShadowSystemClock;
import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link BluetoothMetricsCollector}. */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowSystemClock.class})
public class BluetoothMetricsCollectorTest
    extends SystemMetricsCollectorTest<BluetoothMetrics, BluetoothMetricsCollector> {

  private BluetoothMetricsCollector mBluetoothMetricsCollector;

  @Before
  public void setup() {
    mBluetoothMetricsCollector = new BluetoothMetricsCollector();
  }

  @Test
  public void testNonOpportunisticScan() {
    BluetoothMetrics bluetoothMetrics = new BluetoothMetrics();

    ScanCallback callback = mock(ScanCallback.class);

    // Zero at beginning
    boolean isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(0);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(0);

    // Start scan
    ShadowSystemClock.setUptimeMillis(1);
    mBluetoothMetricsCollector.startScan(callback, false);

    // Intermediate snapshot
    ShadowSystemClock.setUptimeMillis(21);
    isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(1);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(20);

    // Stop scan
    ShadowSystemClock.setUptimeMillis(51);
    mBluetoothMetricsCollector.stopScan(callback);

    // Snapshot at stop
    isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(1);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(50);

    // Snapshot after stop
    ShadowSystemClock.setUptimeMillis(61);
    isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(1);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(50);

    assertThat(bluetoothMetrics.bleOpportunisticScanCount).isEqualTo(0);
    assertThat(bluetoothMetrics.bleOpportunisticScanDurationMs).isEqualTo(0);
  }

  @Test
  public void testOpportunisticScan() {
    BluetoothMetrics bluetoothMetrics = new BluetoothMetrics();
    ScanCallback callback = mock(ScanCallback.class);

    // Zero at beginning
    boolean isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleOpportunisticScanCount).isEqualTo(0);
    assertThat(bluetoothMetrics.bleOpportunisticScanDurationMs).isEqualTo(0);

    // Start scan
    ShadowSystemClock.setUptimeMillis(1);
    mBluetoothMetricsCollector.startScan(callback, true);

    // Intermediate snapshot
    ShadowSystemClock.setUptimeMillis(21);
    isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleOpportunisticScanCount).isEqualTo(1);
    assertThat(bluetoothMetrics.bleOpportunisticScanDurationMs).isEqualTo(20);

    // Stop scan
    ShadowSystemClock.setUptimeMillis(51);
    mBluetoothMetricsCollector.stopScan(callback);

    // Snapshot at stop
    isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleOpportunisticScanCount).isEqualTo(1);
    assertThat(bluetoothMetrics.bleOpportunisticScanDurationMs).isEqualTo(50);

    // Snapshot after stop
    ShadowSystemClock.setUptimeMillis(61);
    isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleOpportunisticScanCount).isEqualTo(1);
    assertThat(bluetoothMetrics.bleOpportunisticScanDurationMs).isEqualTo(50);

    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(0);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(0);
  }

  /**
   * Tests multiple scans that overlap, both opportunistic and non-opportunistic.
   *
   * <pre>
   *   t = 00 01 02 04 08 16 32
   *   A =    [...............]  (Opportunistic)
   *   B =       [.........]     (Non-opportunistic)
   *   C =          [...]        (Non-opportunistic)
   * </pre>
   */
  @Test
  public void testOverlappingScans() {
    BluetoothMetrics bluetoothMetrics = new BluetoothMetrics();

    ScanCallback callbackA = mock(ScanCallback.class);
    ScanCallback callbackB = mock(ScanCallback.class);
    ScanCallback callbackC = mock(ScanCallback.class);

    ShadowSystemClock.setUptimeMillis(1);
    mBluetoothMetricsCollector.startScan(callbackA, true);

    ShadowSystemClock.setUptimeMillis(2);
    mBluetoothMetricsCollector.startScan(callbackB, false);

    ShadowSystemClock.setUptimeMillis(4);
    mBluetoothMetricsCollector.startScan(callbackC, false);

    ShadowSystemClock.setUptimeMillis(8);
    mBluetoothMetricsCollector.stopScan(callbackC);

    // Intermediate snapshot
    mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(2);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(8 - 2);
    assertThat(bluetoothMetrics.bleOpportunisticScanCount).isEqualTo(1);
    assertThat(bluetoothMetrics.bleOpportunisticScanDurationMs).isEqualTo(8 - 1);

    ShadowSystemClock.setUptimeMillis(16);
    mBluetoothMetricsCollector.stopScan(callbackB);

    ShadowSystemClock.setUptimeMillis(32);
    mBluetoothMetricsCollector.stopScan(callbackA);

    // Final snapshot
    ShadowSystemClock.setUptimeMillis(64);
    mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(2);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(16 - 2);
    assertThat(bluetoothMetrics.bleOpportunisticScanCount).isEqualTo(1);
    assertThat(bluetoothMetrics.bleOpportunisticScanDurationMs).isEqualTo(32 - 1);
  }

  @Test
  public void testDuplicateStarts() {
    ScanCallback callbackA = mock(ScanCallback.class);

    ShadowSystemClock.setUptimeMillis(1);
    mBluetoothMetricsCollector.stopScan(mock(ScanCallback.class));
    ShadowSystemClock.setUptimeMillis(2);
    mBluetoothMetricsCollector.startScan(callbackA, false);
    ShadowSystemClock.setUptimeMillis(4);
    mBluetoothMetricsCollector.startScan(callbackA, false);
    ShadowSystemClock.setUptimeMillis(8);
    mBluetoothMetricsCollector.startScan(callbackA, true);
    ShadowSystemClock.setUptimeMillis(16);
    mBluetoothMetricsCollector.stopScan(mock(ScanCallback.class));
    ShadowSystemClock.setUptimeMillis(32);
    mBluetoothMetricsCollector.stopScan(callbackA);
    ShadowSystemClock.setUptimeMillis(64);
    mBluetoothMetricsCollector.stopScan(callbackA);
    ShadowSystemClock.setUptimeMillis(128);

    BluetoothMetrics bluetoothMetrics = new BluetoothMetrics();
    mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(1);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(32 - 2);
    assertThat(bluetoothMetrics.bleOpportunisticScanCount).isEqualTo(0);
    assertThat(bluetoothMetrics.bleOpportunisticScanDurationMs).isEqualTo(0);
  }

  @Override
  protected Class<BluetoothMetricsCollector> getClazz() {
    return BluetoothMetricsCollector.class;
  }
}
