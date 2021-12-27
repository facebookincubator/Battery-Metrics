/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.sensor;

import com.facebook.battery.metrics.sensor.SensorMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SensorMetricsSerializerTest extends SystemMetricsSerializerTest<SensorMetrics> {

  @Override
  protected Class<SensorMetrics> getClazz() {
    return SensorMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<SensorMetrics> getSerializer() {
    return new SensorMetricsSerializer();
  }
}
