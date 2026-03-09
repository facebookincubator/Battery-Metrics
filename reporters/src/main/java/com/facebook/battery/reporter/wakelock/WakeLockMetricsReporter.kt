/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.wakelock

import com.facebook.battery.metrics.core.SystemMetricsLogger
import com.facebook.battery.metrics.wakelock.WakeLockMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter
import org.json.JSONException

class WakeLockMetricsReporter : SystemMetricsReporter<WakeLockMetrics> {

  private var shouldReportAttribution = true

  override fun reportTo(metrics: WakeLockMetrics, event: SystemMetricsReporter.Event) {
    if (metrics.heldTimeMs != 0L) {
      event.add(HELD_TIME_MS, metrics.heldTimeMs)
    }

    if (metrics.acquiredCount != 0L) {
      event.add(ACQUIRED_COUNT, metrics.acquiredCount)
    }

    if (shouldReportAttribution) {
      try {
        val tagAttribution = metrics.attributionToJSONObject()
        if (tagAttribution != null) {
          event.add(TAG_TIME_MS, tagAttribution.toString())
        }
      } catch (ex: JSONException) {
        SystemMetricsLogger.wtf(TAG, "Failed to serialize wakelock attribution data", ex)
      }
    }
  }

  /** Allows selecting if attribution should be included in the logged event. */
  fun setShouldReportAttribution(enabled: Boolean): WakeLockMetricsReporter {
    shouldReportAttribution = enabled
    return this
  }

  companion object {
    @get:JvmStatic val TAG: String = WakeLockMetricsReporter::class.java.simpleName

    const val HELD_TIME_MS: String = "wakelock_held_time_ms"
    const val TAG_TIME_MS: String = "wakelock_tag_time_ms"
    const val ACQUIRED_COUNT: String = "wakelock_acquired_count"
  }
}
