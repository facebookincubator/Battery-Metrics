/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.time

import com.facebook.battery.metrics.core.SystemMetrics

/**
 * Maintain uptime and realtime for the application: remember to use real time for normalizing
 * metrics for comparison.
 */
class TimeMetrics : SystemMetrics<TimeMetrics?>() {

  @JvmField var uptimeMs: Long = 0

  @JvmField var realtimeMs: Long = 0

  override fun set(metrics: TimeMetrics): TimeMetrics {
    this.uptimeMs = metrics.uptimeMs
    this.realtimeMs = metrics.realtimeMs
    return this
  }

  override fun sum(b: TimeMetrics?, output: TimeMetrics?): TimeMetrics {
    var output = output
    if (output == null) {
      output = TimeMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      output.uptimeMs = uptimeMs + b.uptimeMs
      output.realtimeMs = realtimeMs + b.realtimeMs
    }

    return output
  }

  override fun diff(b: TimeMetrics?, output: TimeMetrics?): TimeMetrics {
    var output = output
    if (output == null) {
      output = TimeMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      output.uptimeMs = uptimeMs - b.uptimeMs
      output.realtimeMs = realtimeMs - b.realtimeMs
    }

    return output
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) {
      return true
    }
    if (o == null || javaClass != o.javaClass) {
      return false
    }

    val that = o as TimeMetrics

    return uptimeMs == that.uptimeMs && realtimeMs == that.realtimeMs
  }

  override fun hashCode(): Int {
    var result = (uptimeMs xor (uptimeMs ushr 32)).toInt()
    result = 31 * result + (realtimeMs xor (realtimeMs ushr 32)).toInt()
    return result
  }

  override fun toString(): String = "TimeMetrics{uptimeMs=$uptimeMs, realtimeMs=$realtimeMs}"
}
