/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu

import com.facebook.battery.metrics.core.SystemMetrics

/** Information about system and user cpu metrics: maintains a double per type of CPU time. */
class CpuMetrics : SystemMetrics<CpuMetrics>() {

  @JvmField var userTimeS: Double = 0.0

  @JvmField var systemTimeS: Double = 0.0

  @JvmField var childUserTimeS: Double = 0.0

  @JvmField var childSystemTimeS: Double = 0.0

  override fun set(metrics: CpuMetrics): CpuMetrics {
    this.userTimeS = metrics.userTimeS
    this.systemTimeS = metrics.systemTimeS
    this.childUserTimeS = metrics.childUserTimeS
    this.childSystemTimeS = metrics.childSystemTimeS
    return this
  }

  override fun sum(b: CpuMetrics?, output: CpuMetrics?): CpuMetrics {
    var output = output
    if (output == null) {
      output = CpuMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      output.systemTimeS = systemTimeS + b.systemTimeS
      output.userTimeS = userTimeS + b.userTimeS
      output.childSystemTimeS = childSystemTimeS + b.childSystemTimeS
      output.childUserTimeS = childUserTimeS + b.childUserTimeS
    }

    return output
  }

  override fun diff(b: CpuMetrics?, output: CpuMetrics?): CpuMetrics {
    var output = output
    if (output == null) {
      output = CpuMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      output.systemTimeS = systemTimeS - b.systemTimeS
      output.userTimeS = userTimeS - b.userTimeS
      output.childSystemTimeS = childSystemTimeS - b.childSystemTimeS
      output.childUserTimeS = childUserTimeS - b.childUserTimeS
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

    val that = other as CpuMetrics

    return java.lang.Double.compare(
        that.systemTimeS,
        systemTimeS,
    ) == 0 &&
        java.lang.Double.compare(
            that.userTimeS,
            userTimeS,
        ) == 0 &&
        java.lang.Double.compare(
            that.childSystemTimeS,
            childSystemTimeS,
        ) == 0 &&
        java.lang.Double.compare(that.childUserTimeS, childUserTimeS) == 0
  }

  override fun hashCode(): Int {
    var result: Int
    var temp = java.lang.Double.doubleToLongBits(systemTimeS)
    result = (temp xor (temp ushr 32)).toInt()
    temp = java.lang.Double.doubleToLongBits(userTimeS)
    result = 31 * result + (temp xor (temp ushr 32)).toInt()
    temp = java.lang.Double.doubleToLongBits(childSystemTimeS)
    result = 31 * result + (temp xor (temp ushr 32)).toInt()
    temp = java.lang.Double.doubleToLongBits(childUserTimeS)
    result = 31 * result + (temp xor (temp ushr 32)).toInt()
    return result
  }

  override fun toString(): String =
      "CpuMetrics{userTimeS=${userTimeS}, systemTimeS=${systemTimeS}, childUserTimeS=${childUserTimeS}, childSystemTimeS=${childSystemTimeS}${'}'}"
}
