/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
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
