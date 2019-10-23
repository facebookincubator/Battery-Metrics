/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.devicebattery;

import com.facebook.battery.metrics.devicebattery.DeviceBatteryMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DeviceBatteryMetricsSerializerTest
    extends SystemMetricsSerializerTest<DeviceBatteryMetrics> {

  @Override
  protected Class<DeviceBatteryMetrics> getClazz() {
    return DeviceBatteryMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<DeviceBatteryMetrics> getSerializer() {
    return new com.facebook.battery.serializer.devicebattery.DeviceBatteryMetricsSerializer();
  }
}
