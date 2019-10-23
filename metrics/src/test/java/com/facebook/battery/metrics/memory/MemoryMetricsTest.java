/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.memory;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.core.SystemMetricsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MemoryMetricsTest extends SystemMetricsTest<MemoryMetrics> {

  private MemoryMetrics createMemoryMetrics(int sequenceNumber) {
    MemoryMetrics memoryMetrics = new MemoryMetrics();
    memoryMetrics.sequenceNumber = sequenceNumber;
    memoryMetrics.javaHeapMaxSizeKb = sequenceNumber + 100;
    memoryMetrics.javaHeapAllocatedKb = sequenceNumber + 50;
    memoryMetrics.nativeHeapSizeKb = sequenceNumber + 500;
    memoryMetrics.nativeHeapAllocatedKb = sequenceNumber + 200;
    memoryMetrics.vmSizeKb = sequenceNumber + 1000;
    memoryMetrics.vmRssKb = sequenceNumber + 800;
    return memoryMetrics;
  }

  @Override
  @Test
  public void testSum() throws Exception {
    MemoryMetrics a = createMemoryMetrics(1);
    MemoryMetrics b = createMemoryMetrics(2);
    MemoryMetrics sum = new MemoryMetrics();

    MemoryMetricsCollector collector = new MemoryMetricsCollector();

    collector.getSnapshot(a);
    collector.getSnapshot(b);

    sum = b.sum(a, sum);
    assertThat(sum.sequenceNumber).isEqualTo(b.sequenceNumber);
    assertThat(sum.javaHeapMaxSizeKb).isEqualTo(b.javaHeapMaxSizeKb);
    assertThat(sum.javaHeapAllocatedKb).isEqualTo(b.javaHeapAllocatedKb);
    assertThat(sum.nativeHeapSizeKb).isEqualTo(b.nativeHeapSizeKb);
    assertThat(sum.nativeHeapAllocatedKb).isEqualTo(b.nativeHeapAllocatedKb);
    assertThat(sum.vmSizeKb).isEqualTo(b.vmSizeKb);
    assertThat(sum.vmRssKb).isEqualTo(b.vmRssKb);

    sum = a.sum(b, null);
    assertThat(sum.sequenceNumber).isEqualTo(b.sequenceNumber);
    assertThat(sum.javaHeapMaxSizeKb).isEqualTo(b.javaHeapMaxSizeKb);
    assertThat(sum.javaHeapAllocatedKb).isEqualTo(b.javaHeapAllocatedKb);
    assertThat(sum.nativeHeapSizeKb).isEqualTo(b.nativeHeapSizeKb);
    assertThat(sum.nativeHeapAllocatedKb).isEqualTo(b.nativeHeapAllocatedKb);
    assertThat(sum.vmSizeKb).isEqualTo(b.vmSizeKb);
    assertThat(sum.vmRssKb).isEqualTo(b.vmRssKb);
  }

  @Override
  @Test
  public void testDiff() throws Exception {
    MemoryMetrics a = createMemoryMetrics(1);
    MemoryMetrics b = createMemoryMetrics(2);
    MemoryMetrics sum = new MemoryMetrics();

    MemoryMetricsCollector collector = new MemoryMetricsCollector();

    collector.getSnapshot(a);
    collector.getSnapshot(b);

    sum = b.diff(a, sum);
    assertThat(sum.sequenceNumber).isEqualTo(b.sequenceNumber);
    assertThat(sum.javaHeapMaxSizeKb).isEqualTo(b.javaHeapMaxSizeKb);
    assertThat(sum.javaHeapAllocatedKb).isEqualTo(b.javaHeapAllocatedKb);
    assertThat(sum.nativeHeapSizeKb).isEqualTo(b.nativeHeapSizeKb);
    assertThat(sum.nativeHeapAllocatedKb).isEqualTo(b.nativeHeapAllocatedKb);
    assertThat(sum.vmSizeKb).isEqualTo(b.vmSizeKb);
    assertThat(sum.vmRssKb).isEqualTo(b.vmRssKb);

    sum = a.diff(b, null);
    assertThat(sum.sequenceNumber).isEqualTo(b.sequenceNumber);
    assertThat(sum.javaHeapMaxSizeKb).isEqualTo(b.javaHeapMaxSizeKb);
    assertThat(sum.javaHeapAllocatedKb).isEqualTo(b.javaHeapAllocatedKb);
    assertThat(sum.nativeHeapSizeKb).isEqualTo(b.nativeHeapSizeKb);
    assertThat(sum.nativeHeapAllocatedKb).isEqualTo(b.nativeHeapAllocatedKb);
    assertThat(sum.vmSizeKb).isEqualTo(b.vmSizeKb);
    assertThat(sum.vmRssKb).isEqualTo(b.vmRssKb);
  }

  @Override
  protected Class<MemoryMetrics> getClazz() {
    return MemoryMetrics.class;
  }
}
