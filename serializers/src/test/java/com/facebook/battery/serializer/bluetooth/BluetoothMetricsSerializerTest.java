/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.bluetooth;

import com.facebook.battery.metrics.bluetooth.BluetoothMetrics;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import com.facebook.battery.serializer.common.SystemMetricsSerializerTest;
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
