/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.camera

import android.hardware.Camera
import android.hardware.camera2.CameraDevice
import android.os.SystemClock
import android.util.SparseArray
import androidx.annotation.GuardedBy
import com.facebook.battery.metrics.core.SystemMetricsCollector
import com.facebook.battery.metrics.core.SystemMetricsLogger
import com.facebook.battery.metrics.core.Utilities
import com.facebook.infer.annotation.ThreadSafe

/**
 * CameraMetricsCollector internally maintains how long the camera was open and previewed; this is
 * simply a helper class to maintain state and can't automatically instrument camera usage.
 *
 * The collector exposes camera open/close, preview open/close and error recording functions that
 * can be passed a [Camera] or a [CameraDevice] object and will track times accordingly.
 *
 * - For Camera1, - [Camera#open()], [Camera#open(int)] ->
 *   [recordCameraOpen(Object)] - [Camera#release()] ->
 *   [recordCameraClose(Object)] - [Camera#startPreview()] ->
 *   [recordPreviewStart(Object)] - [Camera#stopPreview()] ->
 *   [recordPreviewStop(Object)] - [Camera.ErrorCallback#onError(int, Camera)] ->
 *   [recordCameraError(Object)] - For
 *   Camera2, - [CameraDevice.StateCallback#onOpened(CameraDevice)] ->
 *   [recordCameraOpen(Object)] - [CameraDevice.StateCallback#onClosed(CameraDevice)] ->
 *   [recordCameraClose(Object)] - [CameraDevice.StateCallback#onError(CameraDevice, int)] ->
 *   [recordCameraError(Object)] - [CameraCaptureSession#setRepeatingRequest(CaptureRequest, CameraCaptureSession.CaptureCallback, Handler)]
 *   -> [recordPreviewStart(Object)] - [CameraCaptureSession#close()] ->
 *   [recordPreviewStop(Object)] - [CameraDevice#close] -> [recordCameraClose(Object)]
 */
@Suppress("deprecation")
@ThreadSafe
class CameraMetricsCollector : SystemMetricsCollector<CameraMetrics>() {

  @GuardedBy("this") private val cameraOpenTimes = SparseArray<Long?>()

  @GuardedBy("this") private val cameraPreviewTimes = SparseArray<Long?>()

  @GuardedBy("this") private var totalCameraOpenTimeMs: Long = 0

  @GuardedBy("this") private var totalCameraPreviewTimeMs: Long = 0

  @GuardedBy("this") private var isEnabled = true

  @Synchronized
  override fun getSnapshot(snapshot: CameraMetrics): Boolean {
    Utilities.checkNotNull(snapshot, "Null value passed to getSnapshot!")
    if (!isEnabled) {
      return false
    }

    val timestampMs = SystemClock.uptimeMillis()
    snapshot.cameraOpenTimeMs = totalCameraOpenTimeMs + sumElapsedTime(timestampMs, cameraOpenTimes)
    snapshot.cameraPreviewTimeMs =
        totalCameraPreviewTimeMs + sumElapsedTime(timestampMs, cameraPreviewTimes)
    return true
  }

  override fun createMetrics(): CameraMetrics = CameraMetrics()

  /**
   * Stop collecting any data and clear all saved information: note that this is only one way and
   * metric collection can't be started again after starting the collector.
   */
  @Synchronized
  fun disable() {
    isEnabled = false

    cameraOpenTimes.clear()
    cameraPreviewTimes.clear()
  }

  @Synchronized
  fun recordCameraOpen(camera: Any) {
    if (!isEnabled) {
      return
    }

    validateArgument(camera)
    startRecord(System.identityHashCode(camera), cameraOpenTimes)
  }

  @Synchronized
  fun recordCameraClose(camera: Any) {
    if (!isEnabled) {
      return
    }

    validateArgument(camera)
    if (isCameraRecording(System.identityHashCode(camera), cameraOpenTimes)) {
      totalCameraOpenTimeMs += stopRecord(System.identityHashCode(camera), cameraOpenTimes)
    }
  }

  @Synchronized
  fun recordPreviewStart(camera: Any) {
    if (!isEnabled) {
      return
    }

    validateArgument(camera)
    startRecord(System.identityHashCode(camera), cameraPreviewTimes)
  }

  @Synchronized
  fun recordPreviewStop(camera: Any) {
    if (!isEnabled) {
      return
    }

    validateArgument(camera)
    totalCameraPreviewTimeMs += stopRecord(System.identityHashCode(camera), cameraPreviewTimes)
  }

  // On a camera error, stop logging for camera open and preview times
  @Synchronized
  fun recordCameraError(camera: Any) {
    if (!isEnabled) {
      return
    }

    validateArgument(camera)
    val cameraHash = System.identityHashCode(camera)
    cameraOpenTimes.delete(cameraHash)
    cameraPreviewTimes.delete(cameraHash)
  }

  companion object {
    private const val TAG = "CameraMetricsCollector"

    @Synchronized
    private fun isCameraRecording(
        hash: Int,
        container: SparseArray<Long?>,
    ): Boolean {
      val startTimeMs = container[hash]
      return startTimeMs != null
    }

    @Synchronized
    private fun startRecord(hash: Int, container: SparseArray<Long?>) {
      val startTimeMs = SystemClock.uptimeMillis()
      if (container[hash] == null) {
        container.append(hash, startTimeMs)
      }
    }

    @Synchronized
    private fun stopRecord(hash: Int, container: SparseArray<Long?>): Long {
      val stopTimeMs = SystemClock.uptimeMillis()
      var totalTimeMs = 0L

      val startTimeMs = container[hash]
      if (startTimeMs != null) {
        totalTimeMs = stopTimeMs - startTimeMs
        container.remove(hash)
      } else {
        SystemMetricsLogger.wtf(
            TAG,
            "Stopped recording details for a camera that hasn't been added yet",
        )
      }
      return totalTimeMs
    }

    private fun validateArgument(camera: Any) {
      require(!(camera !is Camera && camera !is CameraDevice)) {
        "Must pass in a Camera or a CameraDevice"
      }
    }

    private fun sumElapsedTime(timestamp: Long, container: SparseArray<Long?>): Long {
      val size = container.size()
      var sum = 0L
      for (index in 0..<size) {
        sum += timestamp - container.valueAt(index)!!
      }

      return sum
    }
  }
}
