/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
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
