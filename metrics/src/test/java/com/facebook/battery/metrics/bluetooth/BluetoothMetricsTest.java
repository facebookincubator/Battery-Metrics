/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
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
  public void testTwoDiff() {
    BluetoothMetrics metrics = new BluetoothMetrics();

    metrics.bleScanDurationMs = 1000;
    metrics.bleScanCount = 10;

    BluetoothMetrics olderMetrics = new BluetoothMetrics();
    olderMetrics.bleScanCount = 7;
    olderMetrics.bleScanDurationMs = 800;

    BluetoothMetrics deltaMetrics = new BluetoothMetrics();
    deltaMetrics = metrics.diff(olderMetrics, deltaMetrics);

    assertThat(deltaMetrics.bleScanCount).isEqualTo(3);
    assertThat(deltaMetrics.bleScanDurationMs).isEqualTo(200);

    olderMetrics.bleScanCount = 8;
    olderMetrics.bleScanDurationMs = 900;

    deltaMetrics = metrics.diff(olderMetrics, deltaMetrics);

    assertThat(deltaMetrics.bleScanCount).isEqualTo(2);
    assertThat(deltaMetrics.bleScanDurationMs).isEqualTo(100);
  }

  @Override
  protected Class<BluetoothMetrics> getClazz() {
    return BluetoothMetrics.class;
  }
}
