/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.devicebattery;

import com.facebook.battery.metrics.devicebattery.DeviceBatteryMetrics;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DeviceBatteryMetricsSerializer extends SystemMetricsSerializer<DeviceBatteryMetrics> {

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
