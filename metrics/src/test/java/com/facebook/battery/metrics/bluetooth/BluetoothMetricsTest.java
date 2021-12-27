/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.bluetooth;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.core.SystemMetricsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link BluetoothMetrics}. */
@RunWith(RobolectricTestRunner.class)
public class BluetoothMetricsTest extends SystemMetricsTest<BluetoothMetrics> {

  @Test
  public void testEquals() {
    BluetoothMetrics metricsA = new BluetoothMetrics();
    metricsA.bleScanDurationMs = 1000;
    metricsA.bleScanCount = 2;
    metricsA.bleOpportunisticScanDurationMs = 4000;
    metricsA.bleOpportunisticScanCount = 8;

    BluetoothMetrics metricsB = new BluetoothMetrics();
    metricsB.bleScanDurationMs = 1000;
    metricsB.bleScanCount = 2;
    metricsB.bleOpportunisticScanDurationMs = 4000;
    metricsB.bleOpportunisticScanCount = 8;

    assertThat(new BluetoothMetrics()).isEqualTo(new BluetoothMetrics());
    assertThat(metricsA).isEqualTo(metricsB);
  }

  @Test
  public void testDefaultValues() {
    BluetoothMetrics metrics = new BluetoothMetrics();
    assertThat(metrics.bleScanCount).isEqualTo(0);
    assertThat(metrics.bleScanDurationMs).isEqualTo(0);
    assertThat(metrics.bleOpportunisticScanCount).isEqualTo(0);
    assertThat(metrics.bleOpportunisticScanDurationMs).isEqualTo(0);
  }

  @Test
  public void testSet() {
    BluetoothMetrics metrics = new BluetoothMetrics();
    metrics.bleScanDurationMs = 1000;
    metrics.bleScanCount = 10;
    metrics.bleOpportunisticScanDurationMs = 5000;
    metrics.bleOpportunisticScanCount = 3;
    BluetoothMetrics alternate = new BluetoothMetrics();
    alternate.set(metrics);
    assertThat(alternate).isEqualTo(metrics);
  }

  @Test
  public void testDiff() {
    BluetoothMetrics metrics = new BluetoothMetrics();
    metrics.bleScanDurationMs = 1000;
    metrics.bleScanCount = 10;
    metrics.bleOpportunisticScanDurationMs = 5000;
    metrics.bleOpportunisticScanCount = 3;

    BluetoothMetrics olderMetrics = new BluetoothMetrics();
    olderMetrics.bleScanDurationMs = 800;
    olderMetrics.bleScanCount = 7;
    olderMetrics.bleOpportunisticScanDurationMs = 2000;
    olderMetrics.bleOpportunisticScanCount = 1;

    BluetoothMetrics deltaMetrics = new BluetoothMetrics();
    deltaMetrics = metrics.diff(olderMetrics, deltaMetrics);

    assertThat(deltaMetrics.bleScanCount).isEqualTo(3);
    assertThat(deltaMetrics.bleScanDurationMs).isEqualTo(200);
    assertThat(deltaMetrics.bleOpportunisticScanCount).isEqualTo(2);
    assertThat(deltaMetrics.bleOpportunisticScanDurationMs).isEqualTo(3000);
  }

  @Test
  public void testSum() {
    BluetoothMetrics metricsA = new BluetoothMetrics();
    metricsA.bleScanDurationMs = 1000;
    metricsA.bleScanCount = 10;
    metricsA.bleOpportunisticScanDurationMs = 4000;
    metricsA.bleOpportunisticScanCount = 1;

    BluetoothMetrics metricsB = new BluetoothMetrics();
    metricsB.bleScanDurationMs = 2000;
    metricsB.bleScanCount = 20;
    metricsB.bleOpportunisticScanDurationMs = 8000;
    metricsB.bleOpportunisticScanCount = 2;

    BluetoothMetrics output = new BluetoothMetrics();
    metricsA.sum(metricsB, output);

    assertThat(output.bleScanCount).isEqualTo(30);
    assertThat(output.bleScanDurationMs).isEqualTo(3000);
    assertThat(output.bleOpportunisticScanCount).isEqualTo(3);
    assertThat(output.bleOpportunisticScanDurationMs).isEqualTo(12000);
  }

  @Override
  protected Class<BluetoothMetrics> getClazz() {
    return BluetoothMetrics.class;
  }
}
