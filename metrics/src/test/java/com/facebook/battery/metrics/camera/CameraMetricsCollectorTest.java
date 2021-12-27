/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.camera;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import android.hardware.Camera;
import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.ShadowSystemClock;
import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowSystemClock.class})
public class CameraMetricsCollectorTest
    extends SystemMetricsCollectorTest<CameraMetrics, CameraMetricsCollector> {

  @Test
  public void testDisabling() {
    SystemMetricsLogger.Delegate mockDelegate = mock(SystemMetricsLogger.Delegate.class);
    SystemMetricsLogger.setDelegate(mockDelegate);

    CameraMetrics metrics = new CameraMetrics();
    Camera testCamera = Camera.open();
    CameraMetricsCollector collector = new CameraMetricsCollector();

    collector.recordCameraOpen(testCamera);
    assertThat(collector.getSnapshot(metrics)).isTrue();

    collector.disable();
    assertThat(collector.getSnapshot(metrics)).isFalse();

    // Sanity check no exceptions after disabling
    collector.recordPreviewStop(testCamera);
    collector.recordCameraClose(testCamera);
    verifyZeroInteractions(mockDelegate);
  }

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
   * Check when a camera exception happens before its release, we don't record any camera open time.
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
   * Check when a camera exception happens and no camera release is called, we also don't record any
   * camera open time.
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

  /**
   * Check when a camera exception happens and no camera release is called, we also don't record any
   * camera open time.
   */
  @Test
  public void cameraCloseWithoutOpen() {

    SystemMetricsLogger.setDelegate(
        new SystemMetricsLogger.Delegate() {
          @Override
          public void wtf(String Tag, String message, @Nullable Throwable cause) {
            fail(message);
          }
        });

    ShadowSystemClock.setUptimeMillis(200);
    CameraMetricsCollector collector = new CameraMetricsCollector();
    Camera testCamera = Camera.open();
    collector.recordCameraClose(testCamera);

    CameraMetrics snapshot = new CameraMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();
    assertThat(snapshot.cameraOpenTimeMs).isEqualTo(0);
    assertThat(snapshot.cameraPreviewTimeMs).isEqualTo(0);
  }

  @Override
  protected Class<CameraMetricsCollector> getClazz() {
    return CameraMetricsCollector.class;
  }
}
