/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.serializer.memory;

import com.facebook.battery.metrics.memory.MemoryMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MemoryMetricsSerializerTest extends SystemMetricsSerializerTest<MemoryMetrics> {
  @Override
  protected Class<MemoryMetrics> getClazz() {
    return MemoryMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<MemoryMetrics> getSerializer() {
    return new com.facebook.battery.serializer.memory.MemoryMetricsSerializer();
  }
}
