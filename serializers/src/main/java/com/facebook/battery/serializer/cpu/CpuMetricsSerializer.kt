/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.cpu

import com.facebook.battery.metrics.cpu.CpuMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class CpuMetricsSerializer constructor() : SystemMetricsSerializer<CpuMetrics?>() {

  public override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  public override fun serializeContents(metrics: CpuMetrics, output: DataOutput) {
    output.writeDouble(metrics.userTimeS)
    output.writeDouble(metrics.systemTimeS)
    output.writeDouble(metrics.childUserTimeS)
    output.writeDouble(metrics.childSystemTimeS)
  }

  @Throws(IOException::class)
  public override fun deserializeContents(metrics: CpuMetrics, input: DataInput): Boolean {
    metrics.userTimeS = input.readDouble()
    metrics.systemTimeS = input.readDouble()
    metrics.childUserTimeS = input.readDouble()
    metrics.childSystemTimeS = input.readDouble()
    return true
  }

  companion object {
    private const val serialVersionUID: Long = 2_353_414_016_265_691_865L
  }
}
