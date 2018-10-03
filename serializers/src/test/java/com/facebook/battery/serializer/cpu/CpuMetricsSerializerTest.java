/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
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
