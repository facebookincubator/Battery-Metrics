/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.cpu

import com.facebook.battery.metrics.cpu.CpuFrequencyMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class CpuFrequencyMetricsSerializer constructor() :
    SystemMetricsSerializer<CpuFrequencyMetrics?>() {

  public override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  public override fun serializeContents(metrics: CpuFrequencyMetrics, output: DataOutput) {
    val cores = metrics.timeInStateS.size
    output.writeInt(metrics.timeInStateS.size)
    for (i in 0 until cores) {
      val timeInStateS = metrics.timeInStateS.get(i)
      val size = timeInStateS.size()
      output.writeInt(size)
      for (j in 0 until size) {
        output.writeInt(timeInStateS.keyAt(j))
        output.writeInt(timeInStateS.valueAt(j))
      }
    }
  }

  @Throws(IOException::class)
  public override fun deserializeContents(metrics: CpuFrequencyMetrics, input: DataInput): Boolean {
    val cores = input.readInt()
    if (metrics.timeInStateS.size != cores) {
      return false
    }
    for (i in 0 until cores) {
      val size = input.readInt()
      for (j in 0 until size) {
        metrics.timeInStateS.get(i).put(input.readInt(), input.readInt())
      }
    }
    return true
  }

  companion object {
    private const val serialVersionUID: Long = -1_864_103_899_603_750_951L
  }
}
