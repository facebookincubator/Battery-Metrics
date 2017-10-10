/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.cpu;

import com.facebook.battery.metrics.cpu.CpuFrequencyMetrics;
import com.facebook.battery.metrics.cpu.CpuFrequencyMetricsCollector;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import com.facebook.battery.serializer.common.SystemMetricsSerializerTest;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CpuFrequencyMetricsSerializerTest
    extends SystemMetricsSerializerTest<CpuFrequencyMetrics> {

  @BeforeClass
  public static void overrideCpuCores() {
    CpuFrequencyMetricsCollector.overrideCores();
  }

  @Override
  protected Class<CpuFrequencyMetrics> getClazz() {
    return CpuFrequencyMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<CpuFrequencyMetrics> getSerializer() {
    return new CpuFrequencyMetricsSerializer();
  }

  @Override
  protected CpuFrequencyMetrics createInitializedInstance() throws Exception {
    CpuFrequencyMetrics metrics = new CpuFrequencyMetrics();
    metrics.timeInStateS[0].put(100, 100);
    metrics.timeInStateS[0].put(300, 101);
    metrics.timeInStateS[2].put(200, 200);
    return metrics;
  }
}
