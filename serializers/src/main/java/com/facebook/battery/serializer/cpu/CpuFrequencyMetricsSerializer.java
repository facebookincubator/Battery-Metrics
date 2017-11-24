/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.cpu;

import android.util.SparseIntArray;
import com.facebook.battery.metrics.cpu.CpuFrequencyMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CpuFrequencyMetricsSerializer extends SystemMetricsSerializer<CpuFrequencyMetrics> {

  @Override
  public void serializeContents(CpuFrequencyMetrics metrics, DataOutput output) throws IOException {
    int cores = metrics.timeInStateS.length;
    output.writeInt(metrics.timeInStateS.length);
    for (int i = 0; i < cores; i++) {
      SparseIntArray timeInStateS = metrics.timeInStateS[i];
      int size = timeInStateS.size();
      output.writeInt(size);
      for (int j = 0; j < size; j++) {
        output.writeInt(timeInStateS.keyAt(j));
        output.writeInt(timeInStateS.valueAt(j));
      }
    }
  }

  @Override
  public boolean deserializeContents(CpuFrequencyMetrics metrics, DataInput input)
      throws IOException {
    int cores = input.readInt();
    if (metrics.timeInStateS.length != cores) {
      return false;
    }

    for (int i = 0; i < cores; i++) {
      int size = input.readInt();
      for (int j = 0; j < size; j++) {
        metrics.timeInStateS[i].put(input.readInt(), input.readInt());
      }
    }

    return true;
  }
}
