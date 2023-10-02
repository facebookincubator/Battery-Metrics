/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.network

import com.facebook.battery.metrics.network.NetworkMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class NetworkMetricsSerializer : SystemMetricsSerializer<NetworkMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: NetworkMetrics, output: DataOutput) {
    output.writeLong(metrics.mobileBytesRx)
    output.writeLong(metrics.mobileBytesTx)
    output.writeLong(metrics.wifiBytesRx)
    output.writeLong(metrics.wifiBytesTx)
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: NetworkMetrics, input: DataInput): Boolean {
    metrics.mobileBytesRx = input.readLong()
    metrics.mobileBytesTx = input.readLong()
    metrics.wifiBytesRx = input.readLong()
    metrics.wifiBytesTx = input.readLong()
    return true
  }

  companion object {
    private const val serialVersionUID = -2_479_634_339_626_480_691L
  }
}
