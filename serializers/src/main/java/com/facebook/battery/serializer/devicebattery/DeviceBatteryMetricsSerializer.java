/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.devicebattery;

import com.facebook.battery.metrics.devicebattery.DeviceBatteryMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DeviceBatteryMetricsSerializer extends SystemMetricsSerializer<DeviceBatteryMetrics> {

  private static final long serialVersionUID = -2269842438411178483L;

  @Override
  public long getTag() {
    return serialVersionUID;
  }

  @Override
  public void serializeContents(DeviceBatteryMetrics metrics, DataOutput output)
      throws IOException {
    output.writeFloat(metrics.batteryLevelPct);
    output.writeLong(metrics.batteryRealtimeMs);
    output.writeLong(metrics.chargingRealtimeMs);
  }

  @Override
  public boolean deserializeContents(DeviceBatteryMetrics metrics, DataInput input)
      throws IOException {
    metrics.batteryLevelPct = input.readFloat();
    metrics.batteryRealtimeMs = input.readLong();
    metrics.chargingRealtimeMs = input.readLong();
    return true;
  }
}
