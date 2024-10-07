/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.wakelock

import com.facebook.battery.metrics.wakelock.WakeLockMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class WakeLockMetricsSerializer : SystemMetricsSerializer<WakeLockMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: WakeLockMetrics, output: DataOutput) {
    output.writeLong(metrics.heldTimeMs)
    output.writeLong(metrics.acquiredCount)
    output.writeBoolean(metrics.isAttributionEnabled)
    if (metrics.isAttributionEnabled) {
      val size = metrics.tagTimeMs.size()
      output.writeInt(size)
      for (i in 0 until size) {
        val key = metrics.tagTimeMs.keyAt(i)
        val value = metrics.tagTimeMs.valueAt(i)
        output.writeInt(key.length)
        output.writeChars(key)
        output.writeLong(value)
      }
    }
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: WakeLockMetrics, input: DataInput): Boolean {
    metrics.tagTimeMs.clear()

    metrics.heldTimeMs = input.readLong()
    metrics.acquiredCount = input.readLong()
    metrics.isAttributionEnabled = input.readBoolean()
    if (metrics.isAttributionEnabled) {
      val size = input.readInt()
      for (i in 0 until size) {
        val keySize = input.readInt()
        val keyBuilder = StringBuilder()
        for (j in 0 until keySize) {
          keyBuilder.append(input.readChar())
        }
        metrics.tagTimeMs.put(keyBuilder.toString(), input.readLong())
      }
    }

    return true
  }

  companion object {
    private const val serialVersionUID = -153_197_510_099_727_452L
  }
}
