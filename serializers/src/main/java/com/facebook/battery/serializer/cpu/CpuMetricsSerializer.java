/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.serializer.cpu;

import com.facebook.battery.metrics.cpu.CpuMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CpuMetricsSerializer extends SystemMetricsSerializer<CpuMetrics> {

  private static final long serialVersionUID = 2353414016265691865L;

  @Override
  public long getTag() {
    return serialVersionUID;
  }

  @Override
  public void serializeContents(CpuMetrics metrics, DataOutput output) throws IOException {
    output.writeDouble(metrics.userTimeS);
    output.writeDouble(metrics.systemTimeS);
    output.writeDouble(metrics.childUserTimeS);
    output.writeDouble(metrics.childSystemTimeS);
  }

  @Override
  public boolean deserializeContents(CpuMetrics metrics, DataInput input) throws IOException {
    metrics.userTimeS = input.readDouble();
    metrics.systemTimeS = input.readDouble();
    metrics.childUserTimeS = input.readDouble();
    metrics.childSystemTimeS = input.readDouble();
    return true;
  }
}
