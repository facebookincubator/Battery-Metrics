/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.camera;

import com.facebook.battery.metrics.camera.CameraMetrics;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CameraMetricsSerializer extends SystemMetricsSerializer<CameraMetrics> {

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
