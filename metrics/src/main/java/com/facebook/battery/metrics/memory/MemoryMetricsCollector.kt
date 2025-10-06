/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.memory

import android.os.Debug
import android.system.Os
import android.system.OsConstants
import androidx.annotation.GuardedBy
import com.facebook.battery.metrics.core.ProcFileReader
import com.facebook.battery.metrics.core.SystemMetricsCollector
import com.facebook.battery.metrics.core.SystemMetricsLogger
import com.facebook.battery.metrics.core.Utilities
import com.facebook.infer.annotation.ThreadSafe
import java.util.concurrent.atomic.AtomicLong

@ThreadSafe
class MemoryMetricsCollector constructor() : SystemMetricsCollector<MemoryMetrics?>() {

  private val procFileReader = ThreadLocal<ProcFileReader>()
  private val counter = AtomicLong()
  private var pageSizeKb: Long = 4

  @GuardedBy("this") private var isEnabled = false

  @ThreadSafe(enableChecks = false)
  @Synchronized
  override fun getSnapshot(snapshot: MemoryMetrics): Boolean {
    Utilities.checkNotNull(snapshot, "Null value passed to getSnapshot!")
    if (!isEnabled) {
      return false
    }

    /* this helps to track the latest snapshot, diff/sum always picks latest as truth */
    snapshot.sequenceNumber = counter.incrementAndGet()

    snapshot.javaHeapMaxSizeKb = runtimeMaxMemory / KB
    snapshot.javaHeapAllocatedKb = (runtimeTotalMemory - runtimeFreeMemory) / KB

    snapshot.nativeHeapSizeKb = Debug.getNativeHeapSize() / KB
    snapshot.nativeHeapAllocatedKb = Debug.getNativeHeapAllocatedSize() / KB

    snapshot.vmSizeKb = -1
    snapshot.vmRssKb = -1

    try {
      var reader = procFileReader.get()
      if (reader == null) {
        reader = ProcFileReader(path)
        procFileReader.set(reader)
      }

      reader.reset()

      if (reader.isValid) {
        snapshot.vmSizeKb = readField(reader)
        snapshot.vmRssKb = readField(reader)
      }
    } catch (pe: ProcFileReader.ParseException) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse memory (statm) field", pe)
    }

    return true
  }

  @Synchronized
  fun enable() {
    isEnabled = true
  }

  override fun createMetrics(): MemoryMetrics = MemoryMetrics()

  private fun readField(reader: ProcFileReader): Long {
    val memoryKb = reader.readNumber() * pageSizeKb
    reader.skipSpaces()
    return memoryKb
  }

  protected val path: String
    get() = PROC_STAT_FILE_PATH

  protected val runtimeMaxMemory: Long
    get() = Runtime.getRuntime().maxMemory()

  protected val runtimeTotalMemory: Long
    get() = Runtime.getRuntime().totalMemory()

  protected val runtimeFreeMemory: Long
    get() = Runtime.getRuntime().freeMemory()

  init {
    try {
      val pageSizeB = Os.sysconf(OsConstants._SC_PAGESIZE)
      if (pageSizeB > 0) {
        this.pageSizeKb = pageSizeB / KB
      }
    } catch (t: Throwable) {
      // shouldn't happen; fallback to default value
    }
  }

  companion object {
    private const val TAG = "MemoryMetricsCollector"

    private const val PROC_STAT_FILE_PATH = "/proc/self/statm"
    private const val KB = 1_024
  }
}
