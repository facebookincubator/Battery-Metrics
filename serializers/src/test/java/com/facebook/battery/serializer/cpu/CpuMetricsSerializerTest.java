/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.serializer.cpu;

import com.facebook.battery.metrics.cpu.CpuMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CpuMetricsSerializerTest extends SystemMetricsSerializerTest<CpuMetrics> {

  @Override
  protected Class<CpuMetrics> getClazz() {
    return CpuMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<CpuMetrics> getSerializer() {
    return new com.facebook.battery.serializer.cpu.CpuMetricsSerializer();
  }
}
