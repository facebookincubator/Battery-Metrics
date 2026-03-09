/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.memory

import com.facebook.battery.metrics.memory.MemoryMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class MemoryMetricsReporter : SystemMetricsReporter<MemoryMetrics> {

  override fun reportTo(metrics: MemoryMetrics, event: SystemMetricsReporter.Event) {
    if (metrics.javaHeapMaxSizeKb != 0L) {
      event.add(JAVA_HEAP_MAX_SIZE_KB, metrics.javaHeapMaxSizeKb)
    }

    if (metrics.javaHeapAllocatedKb != 0L) {
      event.add(JAVA_HEAP_ALLOCATED_SIZE_KB, metrics.javaHeapAllocatedKb)
    }

    if (metrics.nativeHeapSizeKb != 0L) {
      event.add(NATIVE_HEAP_SIZE_KB, metrics.nativeHeapSizeKb)
    }

    if (metrics.nativeHeapAllocatedKb != 0L) {
      event.add(NATIVE_HEAP_ALLOCATED_SIZE_KB, metrics.nativeHeapAllocatedKb)
    }

    if (metrics.vmSizeKb != 0L) {
      event.add(VM_SIZE_KB, metrics.vmSizeKb)
    }

    if (metrics.vmRssKb != 0L) {
      event.add(VM_RSS_KB, metrics.vmRssKb)
    }
  }

  companion object {
    const val JAVA_HEAP_MAX_SIZE_KB: String = "java_heap_max_size_kb"
    const val JAVA_HEAP_ALLOCATED_SIZE_KB: String = "java_heap_allocated_size_kb"
    const val NATIVE_HEAP_SIZE_KB: String = "native_heap_size_kb"
    const val NATIVE_HEAP_ALLOCATED_SIZE_KB: String = "native_heap_allocated_size_kb"
    const val VM_SIZE_KB: String = "vm_size_kb"
    const val VM_RSS_KB: String = "vm_rss_kb"
  }
}
