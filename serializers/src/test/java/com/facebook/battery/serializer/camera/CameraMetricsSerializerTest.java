/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.serializer.time;

import com.facebook.battery.metrics.camera.CameraMetrics;
import com.facebook.battery.serializer.camera.CameraMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CameraMetricsSerializerTest extends SystemMetricsSerializerTest<CameraMetrics> {

  @Override
  protected Class<CameraMetrics> getClazz() {
    return CameraMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<CameraMetrics> getSerializer() {
    return new CameraMetricsSerializer();
  }
}
