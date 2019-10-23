/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.camera;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.camera.CameraMetrics;
import com.facebook.battery.metrics.core.ReporterEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CameraMetricsReporterTest {
  private final CameraMetricsReporter mCameraMetricsReporter = new CameraMetricsReporter();

  @Test
  public void testZeroLogging() {
    CameraMetrics zeroMetrics = new CameraMetrics();
    ReporterEvent event = new ReporterEvent();
    mCameraMetricsReporter.reportTo(zeroMetrics, event);
    assertThat(event.eventMap.isEmpty()).isTrue();
  }

  @Test
  public void testNonZeroLogging() {
    CameraMetrics metrics1 = new CameraMetrics();
    metrics1.cameraOpenTimeMs = 0;
    metrics1.cameraPreviewTimeMs = 200;
    ReporterEvent event = new ReporterEvent();
    mCameraMetricsReporter.reportTo(metrics1, event);
    assertThat(event.eventMap.get(mCameraMetricsReporter.CAMERA_OPEN_TIME_MS)).isNull();
    assertThat(event.eventMap.get(mCameraMetricsReporter.CAMERA_PREVIEW_TIME_MS)).isEqualTo(200L);

    CameraMetrics metrics2 = new CameraMetrics();
    metrics2.cameraOpenTimeMs = 200;
    metrics2.cameraPreviewTimeMs = 400;
    mCameraMetricsReporter.reportTo(metrics2, event);
    assertThat(event.eventMap.get(mCameraMetricsReporter.CAMERA_OPEN_TIME_MS)).isEqualTo(200L);
    assertThat(event.eventMap.get(mCameraMetricsReporter.CAMERA_PREVIEW_TIME_MS)).isEqualTo(400L);
  }
}
