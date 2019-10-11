/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.memory;

import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;

public class MemoryMetrics extends SystemMetrics<MemoryMetrics> {
  // Memory metrics takes snapshot of current state of memory, since the memory is not
  // monotonically increasing the sequence number to track the freshness. The sum()/diff()
  // method will always return the latest snapshot as result.
  public long sequenceNumber;

  public long javaHeapMaxSizeKb;
  public long javaHeapAllocatedKb;

  public long nativeHeapSizeKb;
  public long nativeHeapAllocatedKb;

  public long vmSizeKb;
  public long vmRssKb;

  public MemoryMetrics() {}

  @Override
  public MemoryMetrics set(MemoryMetrics metrics) {
    javaHeapMaxSizeKb = metrics.javaHeapMaxSizeKb;
    javaHeapAllocatedKb = metrics.javaHeapAllocatedKb;
    nativeHeapSizeKb = metrics.nativeHeapSizeKb;
    nativeHeapAllocatedKb = metrics.nativeHeapAllocatedKb;
    vmSizeKb = metrics.vmSizeKb;
    vmRssKb = metrics.vmRssKb;
    return this;
  }

  public MemoryMetrics sum(@Nullable MemoryMetrics b, @Nullable MemoryMetrics output) {
    if (output == null) {
      output = new MemoryMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      MemoryMetrics latest = sequenceNumber > b.sequenceNumber ? this : b;
      output.sequenceNumber = latest.sequenceNumber;
      output.javaHeapMaxSizeKb = latest.javaHeapMaxSizeKb;
      output.javaHeapAllocatedKb = latest.javaHeapAllocatedKb;
      output.nativeHeapSizeKb = latest.nativeHeapSizeKb;
      output.nativeHeapAllocatedKb = latest.nativeHeapAllocatedKb;
      output.vmSizeKb = latest.vmSizeKb;
      output.vmRssKb = latest.vmRssKb;
    }

    return output;
  }

  @Override
  public MemoryMetrics diff(@Nullable MemoryMetrics b, @Nullable MemoryMetrics output) {
    if (output == null) {
      output = new MemoryMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      MemoryMetrics latest = sequenceNumber >= b.sequenceNumber ? this : b;
      output.sequenceNumber = latest.sequenceNumber;
      output.javaHeapMaxSizeKb = latest.javaHeapMaxSizeKb;
      output.javaHeapAllocatedKb = latest.javaHeapAllocatedKb;
      output.nativeHeapSizeKb = latest.nativeHeapSizeKb;
      output.nativeHeapAllocatedKb = latest.nativeHeapAllocatedKb;
      output.vmSizeKb = latest.vmSizeKb;
      output.vmRssKb = latest.vmRssKb;
    }

    return output;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MemoryMetrics that = (MemoryMetrics) o;

    return javaHeapMaxSizeKb == that.javaHeapMaxSizeKb
        && javaHeapAllocatedKb == that.javaHeapAllocatedKb
        && nativeHeapSizeKb == that.nativeHeapSizeKb
        && nativeHeapAllocatedKb == that.nativeHeapAllocatedKb
        && vmSizeKb == that.vmSizeKb
        && vmRssKb == that.vmRssKb;
  }

  @Override
  public int hashCode() {
    int result = (int) (javaHeapMaxSizeKb ^ (javaHeapMaxSizeKb >>> 32));
    result = 31 * result + (int) (javaHeapAllocatedKb ^ (javaHeapAllocatedKb >>> 32));
    result = 31 * result + (int) (nativeHeapSizeKb ^ (nativeHeapSizeKb >>> 32));
    result = 31 * result + (int) (nativeHeapAllocatedKb ^ (nativeHeapAllocatedKb >>> 32));
    result = 31 * result + (int) (vmSizeKb ^ (vmSizeKb >>> 32));
    result = 31 * result + (int) (vmRssKb ^ (vmRssKb >>> 32));

    return result;
  }

  @Override
  public String toString() {
    return "MemoryMetrics{"
        + "javaHeapMaxSizeKb="
        + javaHeapMaxSizeKb
        + ", javaHeapAllocatedKb="
        + javaHeapAllocatedKb
        + ", nativeHeapSizeKb="
        + nativeHeapSizeKb
        + ", nativeHeapAllocatedKb="
        + nativeHeapAllocatedKb
        + ", vmSizeKb="
        + vmSizeKb
        + ", vmRssKb="
        + vmRssKb
        + "}";
  }
}
