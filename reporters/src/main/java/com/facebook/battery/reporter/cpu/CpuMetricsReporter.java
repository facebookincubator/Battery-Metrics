/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.cpu;

import com.facebook.battery.metrics.cpu.CpuMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.RUNTIME)
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
