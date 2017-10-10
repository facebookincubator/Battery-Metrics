/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.network;

import com.facebook.battery.metrics.network.NetworkMetrics;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import com.facebook.battery.serializer.common.SystemMetricsSerializerTest;
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
