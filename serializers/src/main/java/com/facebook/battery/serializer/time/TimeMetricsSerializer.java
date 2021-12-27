/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.time;

import com.facebook.battery.metrics.time.TimeMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TimeMetricsSerializer extends SystemMetricsSerializer<TimeMetrics> {

  private static final long serialVersionUID = 4345974300167284411L;

  @Override
  public long getTag() {
    return serialVersionUID;
  }

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
