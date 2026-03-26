/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.camera

import com.facebook.battery.metrics.camera.CameraMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class CameraMetricsReporter : SystemMetricsReporter<CameraMetrics> {

  override fun reportTo(metrics: CameraMetrics, event: SystemMetricsReporter.Event) {
    // Do not report value if value is 0
    if (metrics.cameraOpenTimeMs != 0L) {
      event.add(CAMERA_OPEN_TIME_MS, metrics.cameraOpenTimeMs)
    }
    if (metrics.cameraPreviewTimeMs != 0L) {
      event.add(CAMERA_PREVIEW_TIME_MS, metrics.cameraPreviewTimeMs)
    }
  }

  companion object {
    const val CAMERA_OPEN_TIME_MS: String = "camera_open_time_ms"
    const val CAMERA_PREVIEW_TIME_MS: String = "camera_preview_time_ms"
  }
}
