/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.reporter.memory;

import com.facebook.battery.metrics.memory.MemoryMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;

public class MemoryMetricsReporter implements SystemMetricsReporter<MemoryMetrics> {

  public static final String JAVA_HEAP_MAX_SIZE_KB = "java_heap_max_size_kb";
  public static final String JAVA_HEAP_ALLOCATED_SIZE_KB = "java_heap_allocated_size_kb";
  public static final String NATIVE_HEAP_SIZE_KB = "native_heap_size_kb";
  public static final String NATIVE_HEAP_ALLOCATED_SIZE_KB = "native_heap_allocated_size_kb";
  public static final String VM_SIZE_KB = "vm_size_kb";
  public static final String VM_RSS_KB = "vm_rss_kb";

  @Override
  public void reportTo(MemoryMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.javaHeapMaxSizeKb != 0) {
      event.add(JAVA_HEAP_MAX_SIZE_KB, metrics.javaHeapMaxSizeKb);
    }

    if (metrics.javaHeapAllocatedKb != 0) {
      event.add(JAVA_HEAP_ALLOCATED_SIZE_KB, metrics.javaHeapAllocatedKb);
    }

    if (metrics.nativeHeapSizeKb != 0) {
      event.add(NATIVE_HEAP_SIZE_KB, metrics.nativeHeapSizeKb);
    }

    if (metrics.nativeHeapAllocatedKb != 0) {
      event.add(NATIVE_HEAP_ALLOCATED_SIZE_KB, metrics.nativeHeapAllocatedKb);
    }

    if (metrics.vmSizeKb != 0) {
      event.add(VM_SIZE_KB, metrics.vmSizeKb);
    }

    if (metrics.vmRssKb != 0) {
      event.add(VM_RSS_KB, metrics.vmRssKb);
    }
  }
}
