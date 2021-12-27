/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.memory;

import com.facebook.battery.metrics.memory.MemoryMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MemoryMetricsSerializer extends SystemMetricsSerializer<MemoryMetrics> {

  private static final long serialVersionUID = -4040221479651313008L;

  @Override
  public long getTag() {
    return serialVersionUID;
  }

  @Override
  public void serializeContents(MemoryMetrics metrics, DataOutput output) throws IOException {
    output.writeLong(metrics.javaHeapMaxSizeKb);
    output.writeLong(metrics.javaHeapAllocatedKb);
    output.writeLong(metrics.nativeHeapSizeKb);
    output.writeLong(metrics.nativeHeapAllocatedKb);
    output.writeLong(metrics.vmSizeKb);
    output.writeLong(metrics.vmRssKb);
  }

  @Override
  public boolean deserializeContents(MemoryMetrics metrics, DataInput input) throws IOException {
    metrics.javaHeapMaxSizeKb = input.readLong();
    metrics.javaHeapAllocatedKb = input.readLong();
    metrics.nativeHeapSizeKb = input.readLong();
    metrics.nativeHeapAllocatedKb = input.readLong();
    metrics.vmSizeKb = input.readLong();
    metrics.vmRssKb = input.readLong();

    return true;
  }
}
