/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.appwakeup;

import static com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupDetails;
import static com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupReason;

import com.facebook.battery.metrics.appwakeup.AppWakeupMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;

public class AppWakeupMetricSerializerTest extends SystemMetricsSerializerTest<AppWakeupMetrics> {

  @Override
  protected Class<AppWakeupMetrics> getClazz() {
    return AppWakeupMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<AppWakeupMetrics> getSerializer() {
    return new AppWakeupMetricsSerializer();
  }

  @Override
  protected AppWakeupMetrics createInitializedInstance() throws Exception {
    AppWakeupMetrics metrics = new AppWakeupMetrics();
    metrics.appWakeups.put("TestA", new WakeupDetails(WakeupReason.GCM, 10, 20));
    metrics.appWakeups.put("TestB", new WakeupDetails(WakeupReason.JOB_SCHEDULER, 100, 60));
    metrics.appWakeups.put("TestC", new WakeupDetails(WakeupReason.ALARM, 30, 40));
    metrics.appWakeups.put("TestD", new WakeupDetails(WakeupReason.GCM, 23, 27));
    return metrics;
  }
}
