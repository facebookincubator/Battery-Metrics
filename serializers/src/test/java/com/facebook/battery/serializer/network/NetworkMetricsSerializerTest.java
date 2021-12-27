/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.network;

import com.facebook.battery.metrics.network.NetworkMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class NetworkMetricsSerializerTest extends SystemMetricsSerializerTest<NetworkMetrics> {

  @Override
  protected Class<NetworkMetrics> getClazz() {
    return NetworkMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<NetworkMetrics> getSerializer() {
    return new NetworkMetricsSerializer();
  }
}
