/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.camera

import com.facebook.battery.metrics.camera.CameraMetrics
import com.facebook.battery.serializer.core.SystemMetricsSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

class CameraMetricsSerializer : SystemMetricsSerializer<CameraMetrics?>() {

  override fun getTag(): Long = serialVersionUID

  @Throws(IOException::class)
  override fun serializeContents(metrics: CameraMetrics, output: DataOutput) {
    output.writeLong(metrics.cameraOpenTimeMs)
    output.writeLong(metrics.cameraPreviewTimeMs)
  }

  @Throws(IOException::class)
  override fun deserializeContents(metrics: CameraMetrics, input: DataInput): Boolean {
    metrics.cameraOpenTimeMs = input.readLong()
    metrics.cameraPreviewTimeMs = input.readLong()
    return true
  }

  companion object {
    private const val serialVersionUID = -5_544_646_103_548_483_595L
  }
}
