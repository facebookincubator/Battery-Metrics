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
class TimeMetrics : SystemMetrics<TimeMetrics>() {

  @JvmField var uptimeMs: Long = 0

  @JvmField var realtimeMs: Long = 0

  override fun set(p0: TimeMetrics): TimeMetrics {
    this.uptimeMs = p0.uptimeMs
    this.realtimeMs = p0.realtimeMs
    return this
  }

  override fun sum(p0: TimeMetrics?, p1: TimeMetrics?): TimeMetrics {
    var output = p1
    if (output == null) {
      output = TimeMetrics()
    }

    if (p0 == null) {
      output.set(this)
    } else {
      output.uptimeMs = uptimeMs + p0.uptimeMs
      output.realtimeMs = realtimeMs + p0.realtimeMs
    }

    return output
  }

  override fun diff(p0: TimeMetrics?, p1: TimeMetrics?): TimeMetrics {
    var output = p1
    if (output == null) {
      output = TimeMetrics()
    }

    if (p0 == null) {
      output.set(this)
    } else {
      output.uptimeMs = uptimeMs - p0.uptimeMs
      output.realtimeMs = realtimeMs - p0.realtimeMs
    }

    return output
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null || javaClass != other.javaClass) {
      return false
    }

    val that = other as TimeMetrics

    return uptimeMs == that.uptimeMs && realtimeMs == that.realtimeMs
  }

  override fun hashCode(): Int {
    var result = (uptimeMs xor (uptimeMs ushr 32)).toInt()
    result = 31 * result + (realtimeMs xor (realtimeMs ushr 32)).toInt()
    return result
  }

  override fun toString(): String = "TimeMetrics{uptimeMs=$uptimeMs, realtimeMs=$realtimeMs}"
}
