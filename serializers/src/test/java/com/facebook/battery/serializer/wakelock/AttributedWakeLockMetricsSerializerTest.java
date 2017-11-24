/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.wakelock;

import com.facebook.battery.metrics.wakelock.WakeLockMetrics;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import com.facebook.battery.serializer.common.SystemMetricsSerializerTest;

public class AttributedWakeLockMetricsSerializerTest
    extends SystemMetricsSerializerTest<WakeLockMetrics> {

  @Override
  protected Class<WakeLockMetrics> getClazz() {
    return WakeLockMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<WakeLockMetrics> getSerializer() {
    return new WakeLockMetricsSerializer();
  }

  @Override
  protected WakeLockMetrics createInitializedInstance() throws Exception {
    WakeLockMetrics metrics = new WakeLockMetrics(true);
    metrics.heldTimeMs = 12345l;
    metrics.tagTimeMs.put("testA", 100l);
    metrics.tagTimeMs.put("testB", 1000l);
    metrics.tagTimeMs.put("testing", 12345l);
    return metrics;
  }
}
