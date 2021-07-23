/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu;

import static com.facebook.battery.metrics.core.Utilities.checkNotNull;

import androidx.annotation.VisibleForTesting;
import com.facebook.battery.metrics.core.ProcFileReader;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.core.VisibleToAvoidSynthetics;
import com.facebook.infer.annotation.ThreadSafe;

/**
 * Collects data about cpu metrics.
 *
 * <p>This data is read from {@code /proc/<pid>/stat} -- see the corresponding man page for details.
 */
@ThreadSafe
public class CpuMetricsCollector extends SystemMetricsCollector<CpuMetrics> {
  private static final String TAG = "CpuMetricsCollector";
  private static final String PROC_STAT_FILE_PATH = "/proc/self/stat";

  /** See http://man7.org/linux/man-pages/man5/proc.5.html for the indexes and description. */
  private static final int PROC_USER_TIME_FIELD = 13;

  /**
   * Ensure that the cpu metrics value is always increasing: in case the cpu time captured goes
   * down, getSnapshot will return an error to prevent erroneous values. Having it be thread local
   * sidesteps several possible synchronization issues.
   */
  private final ThreadLocal<CpuMetrics> mLastSnapshot = new ThreadLocal<>();

  private final ThreadLocal<ProcFileReader> mProcFileReader = new ThreadLocal<>();

  @VisibleForTesting protected static final long DEFAULT_CLOCK_TICKS_PER_SECOND = 100;

  public CpuMetricsCollector() {}

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(CpuMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");

    try {
      ProcFileReader reader = mProcFileReader.get();
      if (reader == null) {
        reader = new ProcFileReader(getPath());
        mProcFileReader.set(reader);
      }

      reader.reset();

      if (!reader.isValid()) {
        return false;
      }

      reader.skipRightBrace();

      int index = 0;
      while (index < PROC_USER_TIME_FIELD - 1) {
        reader.skipSpaces();
        index++;
      }

      snapshot.userTimeS = readField(reader);
      snapshot.systemTimeS = readField(reader);
      snapshot.childUserTimeS = readField(reader);
      snapshot.childSystemTimeS = readField(reader);
    } catch (ProcFileReader.ParseException pe) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse CPU time field", pe);
      return false;
    }

    if (mLastSnapshot.get() == null) {
      mLastSnapshot.set(new CpuMetrics());
    }

    CpuMetrics lastSnapshot = mLastSnapshot.get();
    if (Double.compare(snapshot.userTimeS, lastSnapshot.userTimeS) < 0
        || Double.compare(snapshot.systemTimeS, lastSnapshot.systemTimeS) < 0
        || Double.compare(snapshot.childUserTimeS, lastSnapshot.childUserTimeS) < 0
        || Double.compare(snapshot.childSystemTimeS, lastSnapshot.childSystemTimeS) < 0) {
      SystemMetricsLogger.wtf(
          TAG, "Cpu Time Decreased from " + lastSnapshot.toString() + " to " + snapshot.toString());
      return false;
    }

    lastSnapshot.set(snapshot);
    return true;
  }

  @Override
  public CpuMetrics createMetrics() {
    return new CpuMetrics();
  }

  static long getClockTicksPerSecond() {
    return Initializer.CLOCK_TICKS_PER_SECOND;
  }

  private static double readField(ProcFileReader reader) {
    double cpuTimeMs = reader.readNumber() * 1.0 / Initializer.CLOCK_TICKS_PER_SECOND;
    reader.skipSpaces();
    return cpuTimeMs;
  }

  protected String getPath() {
    return PROC_STAT_FILE_PATH;
  }

  /**
   * Initialized on demand (https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom)
   * because Collectors can be called/created from any thread.
   */
  private static class Initializer {
    @VisibleToAvoidSynthetics
    static final long CLOCK_TICKS_PER_SECOND = Sysconf.getScClkTck(DEFAULT_CLOCK_TICKS_PER_SECOND);
  }
}
