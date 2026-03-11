/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.time

import com.facebook.battery.metrics.time.TimeMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class TimeMetricsReporter : SystemMetricsReporter<TimeMetrics> {

  override fun reportTo(metrics: TimeMetrics, event: SystemMetricsReporter.Event) {
    if (metrics.realtimeMs != 0L) {
      event.add(REALTIME_MS, metrics.realtimeMs)
    }

    if (metrics.uptimeMs != 0L) {
      event.add(UPTIME_MS, metrics.uptimeMs)
    }
  }

  companion object {
    const val REALTIME_MS: String = "realtime_ms"
    const val UPTIME_MS: String = "uptime_ms"
  }
}
