/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.bluetooth;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;

/** Tests for {@link BluetoothMetricsCollector}. */
@RunWith(RobolectricTestRunner.class)
public class BluetoothMetricsCollectorTest
    extends SystemMetricsCollectorTest<BluetoothMetrics, BluetoothMetricsCollector> {

  private BluetoothMetricsCollector mBluetoothMetricsCollector;

  @Before
  public void setup() {
    mBluetoothMetricsCollector = new BluetoothMetricsCollector();
  }

  @Test
  public void testSnapshot() {
    BluetoothMetrics bluetoothMetrics = new BluetoothMetrics();

    boolean isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(0);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(0);

    mBluetoothMetricsCollector.addScan(100);
    mBluetoothMetricsCollector.addScan(120);

    isSuccess = mBluetoothMetricsCollector.getSnapshot(bluetoothMetrics);
    assertThat(isSuccess).isTrue();
    assertThat(bluetoothMetrics.bleScanCount).isEqualTo(2);
    assertThat(bluetoothMetrics.bleScanDurationMs).isEqualTo(220);
  }

  @Override
  protected Class<BluetoothMetricsCollector> getClazz() {
    return BluetoothMetricsCollector.class;
  }
}
