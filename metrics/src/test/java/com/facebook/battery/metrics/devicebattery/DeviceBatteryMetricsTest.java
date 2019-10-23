/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.devicebattery;

import com.facebook.battery.metrics.core.SystemMetricsTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DeviceBatteryMetricsTest extends SystemMetricsTest<DeviceBatteryMetrics> {

  @Override
  protected Class<DeviceBatteryMetrics> getClazz() {
    return DeviceBatteryMetrics.class;
  }
}
