/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.serializer.camera;

import com.facebook.battery.metrics.camera.CameraMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CameraMetricsSerializer extends SystemMetricsSerializer<CameraMetrics> {

  private static final long serialVersionUID = -5544646103548483595L;

  @Override
  public long getTag() {
    return serialVersionUID;
  }

  @Override
  public void serializeContents(CameraMetrics metrics, DataOutput output) throws IOException {
    output.writeLong(metrics.cameraOpenTimeMs);
    output.writeLong(metrics.cameraPreviewTimeMs);
  }

  @Override
  public boolean deserializeContents(CameraMetrics metrics, DataInput input) throws IOException {
    metrics.cameraOpenTimeMs = input.readLong();
    metrics.cameraPreviewTimeMs = input.readLong();
    return true;
  }
}
