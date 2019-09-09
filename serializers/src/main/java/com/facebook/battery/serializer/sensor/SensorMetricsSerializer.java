/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.serializer.sensor;

import com.facebook.battery.metrics.sensor.SensorMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SensorMetricsSerializer extends SystemMetricsSerializer<SensorMetrics> {

  private static final long serialVersionUID = 1224L;

  @Override
  public long getTag() {
    return serialVersionUID;
  }

  @Override
  public void serializeContents(SensorMetrics metrics, DataOutput output) throws IOException {
    output.writeDouble(metrics.totalPowerMah);
    output.writeLong(metrics.totalActiveTimeMs);
    output.writeLong(metrics.totalWakeUpTimeMs);
  }

  @Override
  public boolean deserializeContents(SensorMetrics metrics, DataInput input) throws IOException {
    metrics.totalPowerMah = input.readDouble();
    metrics.totalActiveTimeMs = input.readLong();
    metrics.totalWakeUpTimeMs = input.readLong();
    return true;
  }
}
