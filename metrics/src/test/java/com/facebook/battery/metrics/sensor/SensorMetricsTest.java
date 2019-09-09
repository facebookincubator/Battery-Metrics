/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.sensor;

import com.facebook.battery.metrics.core.SystemMetricsTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SensorMetricsTest extends SystemMetricsTest<SensorMetrics> {

  @Override
  protected Class<SensorMetrics> getClazz() {
    return SensorMetrics.class;
  }
}
