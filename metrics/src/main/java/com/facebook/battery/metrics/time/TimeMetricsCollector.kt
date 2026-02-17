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
  override fun getSnapshot(snapshot: TimeMetrics): Boolean {
    Utilities.checkNotNull(snapshot, "Null value passed to getSnapshot!")
    // Use elapsedRealtimeNanos and convert to millis to avoid an OEM bug where some
    // manufacturers incorrectly return nanoseconds from elapsedRealtime().
    snapshot.realtimeMs = SystemClock.elapsedRealtimeNanos() / 1_000_000
    snapshot.uptimeMs = SystemClock.uptimeMillis()
    // Uptime (excludes sleep) can never legitimately exceed realtime (includes sleep).
    // If it does, uptimeMillis() likely returned nanos on a buggy OEM device.
    // Use a 1000x threshold to avoid false positives from the small timing gap
    // between the elapsedRealtimeNanos() and uptimeMillis() calls above.
    if (snapshot.uptimeMs > snapshot.realtimeMs * 1000) {
      snapshot.uptimeMs = snapshot.uptimeMs / 1_000_000
    }
    return true
  }

  override fun createMetrics(): TimeMetrics = TimeMetrics()
}
