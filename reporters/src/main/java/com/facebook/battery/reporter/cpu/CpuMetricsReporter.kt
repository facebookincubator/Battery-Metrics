/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.cpu

import com.facebook.battery.metrics.cpu.CpuMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class CpuMetricsReporter : SystemMetricsReporter<CpuMetrics> {

  override fun reportTo(metrics: CpuMetrics, event: SystemMetricsReporter.Event) {
    if (metrics.userTimeS != 0.0) {
      event.add(CPU_USER_TIME_S, metrics.userTimeS)
    }

    if (metrics.systemTimeS != 0.0) {
      event.add(CPU_SYSTEM_TIME_S, metrics.systemTimeS)
    }

    if (metrics.childUserTimeS != 0.0) {
      event.add(CHILD_CPU_USER_TIME_S, metrics.childUserTimeS)
    }

    if (metrics.childSystemTimeS != 0.0) {
      event.add(CHILD_CPU_SYSTEM_TIME_S, metrics.childSystemTimeS)
    }
  }

  companion object {
    const val CPU_USER_TIME_S: String = "cpu_user_time_s"
    const val CPU_SYSTEM_TIME_S: String = "cpu_system_time_s"
    const val CHILD_CPU_USER_TIME_S: String = "child_cpu_user_time_s"
    const val CHILD_CPU_SYSTEM_TIME_S: String = "child_cpu_system_time_s"
  }
}
