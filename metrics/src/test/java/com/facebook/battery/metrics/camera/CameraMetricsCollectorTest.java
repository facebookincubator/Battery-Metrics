/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.camera;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.hardware.Camera;
import com.facebook.battery.metrics.common.ShadowSystemClock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(shadows = {ShadowSystemClock.class})
public class CameraMetricsCollectorTest {

  @Test
  public void testSimpleOpenSnapshot() {
    ShadowSystemClock.setUptimeMillis(200);
    CameraMetricsCollector collector = new CameraMetricsCollector();
    Camera testCamera = Camera.open();
    collector.recordCameraOpen(testCamera);

    ShadowSystemClock.setUptimeMillis(1000);
    collector.recordCameraClose(testCamera);

    CameraMetrics snapshot = new CameraMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();
    assertThat(snapshot.cameraOpenTimeMs).isEqualTo(800);
    assertThat(snapshot.cameraPreviewTimeMs).isEqualTo(0);
  }

  @Test
  public void testSimplePreviewSnapshot() {
    ShadowSystemClock.setUptimeMillis(700);
    CameraMetricsCollector collector = new CameraMetricsCollector();
    Camera testCamera = Camera.open();
    collector.recordPreviewStart(testCamera);

    ShadowSystemClock.setUptimeMillis(1000);
    collector.recordPreviewStop(testCamera);

    CameraMetrics snapshot = new CameraMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();
    assertThat(snapshot.cameraOpenTimeMs).isEqualTo(0);
    assertThat(snapshot.cameraPreviewTimeMs).isEqualTo(300);
  }

  @Test
  public void testIncompleteOpenSnapshot() {
    ShadowSystemClock.setUptimeMillis(500);
    CameraMetricsCollector collector = new CameraMetricsCollector();
    Camera testCamera = Camera.open();
    collector.recordCameraOpen(testCamera);

    ShadowSystemClock.setUptimeMillis(1000);
    CameraMetrics snapshot = new CameraMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();
    assertThat(snapshot.cameraOpenTimeMs).isEqualTo(500);
    assertThat(snapshot.cameraPreviewTimeMs).isEqualTo(0);
  }

  @Test
  public void testIncompletePreviewSnapshot() {
    ShadowSystemClock.setUptimeMillis(700);
    CameraMetricsCollector collector = new CameraMetricsCollector();
    Camera testCamera = Camera.open();
    collector.recordPreviewStart(testCamera);

    ShadowSystemClock.setUptimeMillis(1000);
    CameraMetrics snapshot = new CameraMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();
    assertThat(snapshot.cameraOpenTimeMs).isEqualTo(0);
    assertThat(snapshot.cameraPreviewTimeMs).isEqualTo(300);
  }

  /**
   * Check when a camera exception happens before its release, we don't
   * record any camera open time.
   */
  @Test
  public void testCameraOpenExceptionWithRelease() {
    ShadowSystemClock.setUptimeMillis(200);
    CameraMetricsCollector collector = new CameraMetricsCollector();
    Camera testCamera = Camera.open();
    collector.recordCameraOpen(testCamera);

    ShadowSystemClock.setUptimeMillis(1000);
    collector.recordCameraClose(testCamera);

    ShadowSystemClock.setUptimeMillis(1500);
    collector.recordCameraOpen(testCamera);

    ShadowSystemClock.setUptimeMillis(1800);
    collector.recordCameraError(testCamera);

    CameraMetrics snapshot = new CameraMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();
    assertThat(snapshot.cameraOpenTimeMs).isEqualTo(800);
    assertThat(snapshot.cameraPreviewTimeMs).isEqualTo(0);
  }

  /**
   * Check when a camera exception happens and no camera release is called,
   * we also don't record any camera open time.
   */
  @Test
  public void testCameraOpenExceptionWithoutRelease() {
    ShadowSystemClock.setUptimeMillis(200);
    CameraMetricsCollector collector = new CameraMetricsCollector();
    Camera testCamera = Camera.open();
    collector.recordCameraOpen(testCamera);
    collector.recordPreviewStart(testCamera);

    ShadowSystemClock.setUptimeMillis(1000);
    collector.recordCameraError(testCamera);

    // open the camera again after the exception
    collector.recordCameraOpen(testCamera);

    ShadowSystemClock.setUptimeMillis(1500);
    collector.recordPreviewStart(testCamera);

    ShadowSystemClock.setUptimeMillis(1800);
    collector.recordPreviewStop(testCamera);

    CameraMetrics snapshot = new CameraMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();
    assertThat(snapshot.cameraOpenTimeMs).isEqualTo(800);
    assertThat(snapshot.cameraPreviewTimeMs).isEqualTo(300);
  }
}
