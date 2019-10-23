/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.bluetooth;

import com.facebook.battery.metrics.bluetooth.BluetoothMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BluetoothMetricsSerializerTest extends SystemMetricsSerializerTest<BluetoothMetrics> {

  @Override
  protected Class<BluetoothMetrics> getClazz() {
    return BluetoothMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<BluetoothMetrics> getSerializer() {
    return new BluetoothMetricsSerializer();
  }
}
