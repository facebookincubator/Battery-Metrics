/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.time;

import com.facebook.battery.metrics.time.TimeMetrics;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TimeMetricsSerializer extends SystemMetricsSerializer<TimeMetrics> {

  @Override
  public void serializeContents(TimeMetrics metrics, DataOutput output) throws IOException {
    output.writeLong(metrics.realtimeMs);
    output.writeLong(metrics.uptimeMs);
  }

  @Override
  public boolean deserializeContents(TimeMetrics metrics, DataInput input) throws IOException {
    metrics.realtimeMs = input.readLong();
    metrics.uptimeMs = input.readLong();
    return true;
  }
}
