/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.camera

import com.facebook.battery.metrics.core.SystemMetrics

class CameraMetrics : SystemMetrics<CameraMetrics>() {

  @JvmField var cameraPreviewTimeMs: Long = 0

  @JvmField var cameraOpenTimeMs: Long = 0

  override fun sum(b: CameraMetrics?, output: CameraMetrics?): CameraMetrics {
    var output = output
    if (output == null) {
      output = CameraMetrics()
    }
    if (b == null) {
      output.set(this)
    } else {
      output.cameraPreviewTimeMs = cameraPreviewTimeMs + b.cameraPreviewTimeMs
      output.cameraOpenTimeMs = cameraOpenTimeMs + b.cameraOpenTimeMs
    }

    return output
  }

  override fun diff(b: CameraMetrics?, output: CameraMetrics?): CameraMetrics {
    var output = output
    if (output == null) {
      output = CameraMetrics()
    }
    if (b == null) {
      output.set(this)
    } else {
      output.cameraPreviewTimeMs = cameraPreviewTimeMs - b.cameraPreviewTimeMs
      output.cameraOpenTimeMs = cameraOpenTimeMs - b.cameraOpenTimeMs
    }

    return output
  }

  override fun set(b: CameraMetrics): CameraMetrics {
    this.cameraPreviewTimeMs = b.cameraPreviewTimeMs
    this.cameraOpenTimeMs = b.cameraOpenTimeMs
    return this
  }

  override fun toString(): String =
      "CameraMetrics{cameraPreviewTimeMs=${cameraPreviewTimeMs}, cameraOpenTimeMs=${cameraOpenTimeMs}${'}'}"

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null || javaClass != other.javaClass) {
      return false
    }

    val that = other as CameraMetrics
    return cameraPreviewTimeMs == that.cameraPreviewTimeMs &&
        cameraOpenTimeMs == that.cameraOpenTimeMs
  }

  override fun hashCode(): Int {
    var result = (this.cameraPreviewTimeMs xor (this.cameraPreviewTimeMs ushr 32)).toInt()
    result = 31 * result + (cameraOpenTimeMs xor (cameraOpenTimeMs ushr 32)).toInt()
    return result
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
