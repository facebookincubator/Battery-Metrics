/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.memory;

import static com.facebook.battery.metrics.core.Utilities.checkNotNull;

import android.os.Debug;
import com.facebook.battery.metrics.core.ProcFileReader;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.infer.annotation.ThreadSafe;
import java.util.concurrent.atomic.AtomicLong;

@ThreadSafe
public class MemoryMetricsCollector extends SystemMetricsCollector<MemoryMetrics> {
  private static final String TAG = "MemoryMetricsCollector";

  private static final String PROC_STAT_FILE_PATH = "/proc/self/statm";
  private static final int KB = 1024;
  private static final int PAGE_SIZE_KB = 4;

  private final ThreadLocal<ProcFileReader> mProcFileReader = new ThreadLocal<>();
  private final AtomicLong mCounter = new AtomicLong();

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(MemoryMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");

    /* this helps to track the latest snapshot, diff/sum always picks latest as truth */
    snapshot.sequenceNumber = mCounter.incrementAndGet();

    Runtime runtime = getRuntime();
    snapshot.javaHeapMaxSizeKb = runtime.maxMemory() / KB;
    snapshot.javaHeapAllocatedKb = (runtime.totalMemory() - runtime.freeMemory()) / KB;

    snapshot.nativeHeapSizeKb = Debug.getNativeHeapSize() / KB;
    snapshot.nativeHeapAllocatedKb = Debug.getNativeHeapAllocatedSize() / KB;

    snapshot.vmSizeKb = -1;
    snapshot.vmRssKb = -1;

    try {
      ProcFileReader reader = mProcFileReader.get();
      if (reader == null) {
        reader = new ProcFileReader(getPath());
        mProcFileReader.set(reader);
      }

      reader.reset();

      if (reader.isValid()) {
        snapshot.vmSizeKb = readField(reader);
        snapshot.vmRssKb = readField(reader);
      }
    } catch (ProcFileReader.ParseException pe) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse memory (statm) field", pe);
    }

    return true;
  }

  @Override
  public MemoryMetrics createMetrics() {
    return new MemoryMetrics();
  }

  private static long readField(ProcFileReader reader) {
    long memoryKb = reader.readNumber() * PAGE_SIZE_KB;
    reader.skipSpaces();
    return memoryKb;
  }

  protected String getPath() {
    return PROC_STAT_FILE_PATH;
  }

  protected Runtime getRuntime() {
    return Runtime.getRuntime();
  }
}
