/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.cpu

import androidx.annotation.VisibleForTesting
import com.facebook.battery.metrics.cpu.CpuFrequencyMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

/**
 * Reports cpu statuses to an event. As a simplification, the CpuFrequencyMetricsCollector collects
 * and maintains information about each cpu instead of the cluster of cpus it belongs to because of
 * varying levels of topology file access observed across different phones.
 *
 * The reporter groups equal frequency values together into a cpumask (similar to that reported by
 * the topology sysfs files) mapped to the corresponding frequency.
 */
class CpuFrequencyMetricsReporter : SystemMetricsReporter<CpuFrequencyMetrics> {

  override fun reportTo(metrics: CpuFrequencyMetrics, event: SystemMetricsReporter.Event) {
    val output = metrics.toJSONObject()
    if (output != null && output.length() != 0) {
      event.add(CPU_TIME_IN_STATE_S, output.toString())
    }
  }

  companion object {
    @VisibleForTesting const val CPU_TIME_IN_STATE_S: String = "cpu_time_in_state_s"
  }
}
