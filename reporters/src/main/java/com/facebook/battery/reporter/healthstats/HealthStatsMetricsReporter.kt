/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.healthstats

import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.battery.metrics.core.SystemMetricsLogger
import com.facebook.battery.metrics.healthstats.HealthStatsMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter
import org.json.JSONException

@RequiresApi(api = Build.VERSION_CODES.N)
class HealthStatsMetricsReporter : SystemMetricsReporter<HealthStatsMetrics> {

  override fun reportTo(metrics: HealthStatsMetrics, event: SystemMetricsReporter.Event) {
    try {
      event.add(HEALTHSTATS, metrics.toJSONObject().toString())
    } catch (jsone: JSONException) {
      SystemMetricsLogger.wtf(
          "HealthStatsMetricsReporter",
          "Couldn't log healthstats metrics",
          jsone,
      )
    }
  }

  companion object {
    private const val HEALTHSTATS = "healthstats"
  }
}
