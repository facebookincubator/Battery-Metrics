/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.camera;

import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class CameraMetrics extends SystemMetrics<CameraMetrics> {

  public long cameraPreviewTimeMs;
  public long cameraOpenTimeMs;

  @Override
  public CameraMetrics sum(@Nullable CameraMetrics b, @Nullable CameraMetrics output) {
    if (output == null) {
      output = new CameraMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      output.cameraPreviewTimeMs = cameraPreviewTimeMs + b.cameraPreviewTimeMs;
      output.cameraOpenTimeMs = cameraOpenTimeMs + b.cameraOpenTimeMs;
    }

    return output;
  }

  @Override
  public CameraMetrics diff(@Nullable CameraMetrics b, @Nullable CameraMetrics output) {
    if (output == null) {
      output = new CameraMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      output.cameraPreviewTimeMs = cameraPreviewTimeMs - b.cameraPreviewTimeMs;
      output.cameraOpenTimeMs = cameraOpenTimeMs - b.cameraOpenTimeMs;
    }

    return output;
  }

  @Override
  public CameraMetrics set(CameraMetrics b) {
    cameraPreviewTimeMs = b.cameraPreviewTimeMs;
    cameraOpenTimeMs = b.cameraOpenTimeMs;
    return this;
  }

  @Override
  public String toString() {
    return "CameraMetrics{"
        + "cameraPreviewTimeMs="
        + cameraPreviewTimeMs
        + ", cameraOpenTimeMs="
        + cameraOpenTimeMs
        + '}';
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CameraMetrics that = (CameraMetrics) o;
    return cameraPreviewTimeMs == that.cameraPreviewTimeMs
        && cameraOpenTimeMs == that.cameraOpenTimeMs;
  }

  @Override
  public int hashCode() {
    int result = (int) (cameraPreviewTimeMs ^ (cameraPreviewTimeMs >>> 32));
    result = 31 * result + (int) (cameraOpenTimeMs ^ (cameraOpenTimeMs >>> 32));
    return result;
  }
}
