/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.cpu;

import com.facebook.battery.metrics.cpu.CpuMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;

public class CpuMetricsReporter implements SystemMetricsReporter<CpuMetrics> {

  public static final String CPU_USER_TIME_S = "cpu_user_time_s";
  public static final String CPU_SYSTEM_TIME_S = "cpu_system_time_s";
  public static final String CHILD_CPU_USER_TIME_S = "child_cpu_user_time_s";
  public static final String CHILD_CPU_SYSTEM_TIME_S = "child_cpu_system_time_s";

  @Override
  public void reportTo(CpuMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.userTimeS != 0) {
      event.add(CPU_USER_TIME_S, metrics.userTimeS);
    }

    if (metrics.systemTimeS != 0) {
      event.add(CPU_SYSTEM_TIME_S, metrics.systemTimeS);
    }

    if (metrics.childUserTimeS != 0) {
      event.add(CHILD_CPU_USER_TIME_S, metrics.childUserTimeS);
    }

    if (metrics.childSystemTimeS != 0) {
      event.add(CHILD_CPU_SYSTEM_TIME_S, metrics.childSystemTimeS);
    }
  }
}
