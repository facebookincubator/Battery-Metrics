/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.cpu;

import com.facebook.battery.metrics.cpu.CpuMetrics;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CpuMetricsSerializer extends SystemMetricsSerializer<CpuMetrics> {

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
