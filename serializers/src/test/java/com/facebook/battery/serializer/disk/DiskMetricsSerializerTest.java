/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.serializer.disk;

import com.facebook.battery.metrics.disk.DiskMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DiskMetricsSerializerTest extends SystemMetricsSerializerTest<DiskMetrics> {

  @Override
  protected Class<DiskMetrics> getClazz() {
    return DiskMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<DiskMetrics> getSerializer() {
    return new com.facebook.battery.serializer.disk.DiskMetricsSerializer();
  }
}
