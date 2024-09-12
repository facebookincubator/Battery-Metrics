/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.appwakeup

import com.facebook.battery.metrics.appwakeup.AppWakeupMetrics
import com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupDetails
import com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupReason
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class AppWakeupMetricsSerializer : SystemMetricsSerializer<AppWakeupMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: AppWakeupMetrics, output: DataOutput) {
    output.writeInt(metrics.appWakeups.size())
    for (i in 0 until metrics.appWakeups.size()) {
      val wakeupName = metrics.appWakeups.keyAt(i)
      val details = metrics.appWakeups.valueAt(i)
      output.writeInt(wakeupName.length)
      output.writeChars(wakeupName)
      output.writeInt(details.reason.ordinal)
      output.writeLong(details.count)
      output.writeLong(details.wakeupTimeMs)
    }
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: AppWakeupMetrics, input: DataInput): Boolean {
    metrics.appWakeups.clear()
    val size = input.readInt()
    for (i in 0 until size) {
      val wakeupName = readChars(input, input.readInt())
      val reason = WakeupReason.entries[input.readInt()]
      val count = input.readLong()
      val timeMs = input.readLong()
      metrics.appWakeups.put(wakeupName, WakeupDetails(reason, count, timeMs))
    }
    return true
  }

  @Throws(IOException::class)
  private fun readChars(input: DataInput, len: Int): String {
    val builder = StringBuilder()
    for (i in 0 until len) {
      builder.append(input.readChar())
    }
    return builder.toString()
  }

  companion object {
    private const val serialVersionUID = -3_421_285_698_064_072_703L
  }
}
