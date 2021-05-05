/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.camera;

import com.facebook.battery.metrics.camera.CameraMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class CameraMetricsReporter implements SystemMetricsReporter<CameraMetrics> {

  public static final String CAMERA_OPEN_TIME_MS = "camera_open_time_ms";
  public static final String CAMERA_PREVIEW_TIME_MS = "camera_preview_time_ms";

  @Override
  public void reportTo(CameraMetrics metrics, SystemMetricsReporter.Event event) {
    // Do not report value if value is 0
    if (metrics.cameraOpenTimeMs != 0) {
      event.add(CAMERA_OPEN_TIME_MS, metrics.cameraOpenTimeMs);
    }
    if (metrics.cameraPreviewTimeMs != 0) {
      event.add(CAMERA_PREVIEW_TIME_MS, metrics.cameraPreviewTimeMs);
    }
  }
}
