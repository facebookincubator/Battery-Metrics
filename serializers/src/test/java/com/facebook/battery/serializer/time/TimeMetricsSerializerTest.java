/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.time;

import com.facebook.battery.metrics.time.TimeMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TimeMetricsSerializerTest extends SystemMetricsSerializerTest<TimeMetrics> {

  @Override
  protected Class<TimeMetrics> getClazz() {
    return TimeMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<TimeMetrics> getSerializer() {
    return new TimeMetricsSerializer();
  }
}
