/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.camera;

import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.SparseArray;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.infer.annotation.ThreadSafe;

/**
 * CameraMetricsCollector internally maintains how long the camera was open and previewed; this is
 * simply a helper class to maintain state and can't automatically instrument camera usage.
 *
 * <p>The collector exposes camera open/close, preview open/close and error recording functions that
 * can be passed a {@link Camera} or a {@link CameraDevice} object and will track times accordingly.
 *
 * <p>- For Camera1, - {@link Camera#open()}, {@link Camera#open(int)} -> {@link
 * #recordCameraOpen(Object)} - {@link Camera#release()} -> {@link #recordCameraClose(Object)} -
 * {@link Camera#startPreview()} -> {@link #recordPreviewStart(Object)} - {@link
 * Camera#stopPreview()} -> {@link #recordPreviewStop(Object)} - {@link
 * Camera.ErrorCallback#onError(int, Camera)} -> {@link #recordCameraError(Object)} - For Camera2, -
 * {@link CameraDevice.StateCallback#onOpened(CameraDevice)} -> {@link #recordCameraOpen(Object)} -
 * {@link CameraDevice.StateCallback#onClosed(CameraDevice)} -> {@link #recordCameraClose(Object)} -
 * {@link CameraDevice.StateCallback#onError(CameraDevice, int)} -> {@link
 * #recordCameraError(Object)} - {@link CameraCaptureSession#setRepeatingRequest(CaptureRequest,
 * CameraCaptureSession.CaptureCallback, Handler)} -> {@link #recordPreviewStart(Object)} - {@link
 * CameraCaptureSession#close()} -> {@link #recordPreviewStop(Object)} - {@link CameraDevice#close}
 * -> {@link #recordCameraClose(Object)}
 */
@ThreadSafe
public class CameraMetricsCollector extends SystemMetricsCollector<CameraMetrics> {
  private static final String TAG = "CameraMetricsCollector";

  private final SparseArray<Long> mCameraOpenTimes = new SparseArray<>();
  private final SparseArray<Long> mCameraPreviewTimes = new SparseArray<>();

  private long mTotalCameraOpenTimeMs;
  private long mTotalCameraPreviewTimeMs;

  public CameraMetricsCollector() {}

  @Override
  public synchronized boolean getSnapshot(CameraMetrics snapshot) {
    long timestampMs = SystemClock.uptimeMillis();
    snapshot.cameraOpenTimeMs =
        mTotalCameraOpenTimeMs + sumElapsedTime(timestampMs, mCameraOpenTimes);
    snapshot.cameraPreviewTimeMs =
        mTotalCameraPreviewTimeMs + sumElapsedTime(timestampMs, mCameraPreviewTimes);
    return true;
  }

  @Override
  public CameraMetrics createMetrics() {
    return new CameraMetrics();
  }

  public synchronized void recordCameraOpen(Object camera) {
    validateArgument(camera);
    startRecord(System.identityHashCode(camera), mCameraOpenTimes);
  }

  public synchronized void recordCameraClose(Object camera) {
    validateArgument(camera);
    mTotalCameraOpenTimeMs += stopRecord(System.identityHashCode(camera), mCameraOpenTimes);
  }

  public synchronized void recordPreviewStart(Object camera) {
    validateArgument(camera);
    startRecord(System.identityHashCode(camera), mCameraPreviewTimes);
  }

  public synchronized void recordPreviewStop(Object camera) {
    validateArgument(camera);
    mTotalCameraPreviewTimeMs += stopRecord(System.identityHashCode(camera), mCameraPreviewTimes);
  }

  // On a camera error, stop logging for camera open and preview times
  public synchronized void recordCameraError(Object camera) {
    validateArgument(camera);
    int cameraHash = System.identityHashCode(camera);
    mCameraOpenTimes.delete(cameraHash);
    mCameraPreviewTimes.delete(cameraHash);
  }

  private static synchronized void startRecord(int hash, SparseArray<Long> container) {
    long startTimeMs = SystemClock.uptimeMillis();
    if (container.get(hash) == null) {
      container.append(hash, startTimeMs);
    }
  }

  private static synchronized long stopRecord(int hash, SparseArray<Long> container) {
    long stopTimeMs = SystemClock.uptimeMillis();
    long totalTimeMs = 0;

    Long startTimeMs = container.get(hash);
    if (startTimeMs != null) {
      totalTimeMs = stopTimeMs - startTimeMs;
      container.remove(hash);
    } else {
      SystemMetricsLogger.wtf(
          TAG, "Stopped recording details for a camera that hasn't been added yet");
    }
    return totalTimeMs;
  }

  private static void validateArgument(Object camera) {
    if (!(camera instanceof Camera)
        && (Build.VERSION.SDK_INT < 21 || !(camera instanceof CameraDevice))) {
      throw new IllegalArgumentException("Must pass in a Camera or a CameraDevice");
    }
  }

  private static long sumElapsedTime(long timestamp, SparseArray<Long> container) {
    int size = container.size();
    long sum = 0;
    for (int index = 0; index < size; index++) {
      sum += timestamp - container.valueAt(index);
    }

    return sum;
  }
}
