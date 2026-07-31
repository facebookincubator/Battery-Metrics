/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.appwakeup

import com.facebook.battery.metrics.appwakeup.AppWakeupMetrics
import com.facebook.battery.metrics.core.SystemMetricsLogger
import com.facebook.battery.reporter.core.SystemMetricsReporter
import org.json.JSONException

class AppWakeupMetricsReporter : SystemMetricsReporter<AppWakeupMetrics> {

  override fun reportTo(metrics: AppWakeupMetrics, event: SystemMetricsReporter.Event) {
    try {
      val representation = metrics.toJSON()
      if (representation != null) {
        event.add(APP_WAKEUPS, checkNotNull(representation.toString()))
      }
    } catch (jsone: JSONException) {
      SystemMetricsLogger.wtf(TAG, "Unable to report AppWakeupMetrics", jsone)
    }
  }

  companion object {
    private const val TAG = "AppWakeupMetricsReporter"

    private const val APP_WAKEUPS = "app_wakeup_attribution"
  }
}
