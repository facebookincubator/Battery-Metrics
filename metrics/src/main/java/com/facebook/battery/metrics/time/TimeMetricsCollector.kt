/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.time

import android.os.SystemClock
import com.facebook.battery.metrics.core.SystemMetricsCollector
import com.facebook.battery.metrics.core.Utilities
import com.facebook.infer.annotation.ThreadSafe

/**
 * Records system uptime (doesn't include time when the phone was asleep) and realtime (actual time
 * elapsed). This is a fairly core collector that can be used to normalize the values obtained by
 * all other collectors for comparison.
 */
@ThreadSafe
class TimeMetricsCollector : SystemMetricsCollector<TimeMetrics?>() {

  @ThreadSafe(enableChecks = false)
  override fun getSnapshot(p0: TimeMetrics): Boolean {
    Utilities.checkNotNull(p0, "Null value passed to getSnapshot!")
    p0.realtimeMs = SystemClock.elapsedRealtime()
    p0.uptimeMs = SystemClock.uptimeMillis()
    return true
  }

  override fun createMetrics(): TimeMetrics = TimeMetrics()
}
