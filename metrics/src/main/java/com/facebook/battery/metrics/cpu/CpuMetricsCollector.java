/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.cpu;

import static com.facebook.battery.metrics.core.Utilities.checkNotNull;

import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.core.VisibleToAvoidSynthetics;
import com.facebook.infer.annotation.ThreadSafe;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Collects data about cpu metrics.
 *
 * <p>This data is read from {@code /proc/<pid>/stat} -- see the corresponding man page for details.
 */
@ThreadSafe
public class CpuMetricsCollector extends SystemMetricsCollector<CpuMetrics> {
  private static final String TAG = "CpuMetricsCollector";
  private static final String PROC_STAT_FILE_PATH = "/proc/self/stat";

  /** See http://man7.org/linux/man-pages/man5/proc.5.html for a description of these fields. */
  private static final int PROC_USER_TIME_FIELD = 13;

  private static final int PROC_SYSTEM_TIME_FIELD = 14;
  private static final int PROC_CHILD_USER_TIME_FIELD = 15;
  private static final int PROC_CHILD_SYSTEM_TIME_FIELD = 16;

  /**
   * Ensure that the cpu metrics value is always increasing: in case the cpu time captured goes
   * down, getSnapshot will return an error to prevent erroneous values. Having it be thread local
   * sidesteps several possible synchronization issues.
   */
  private final ThreadLocal<CpuMetrics> mLastSnapshot = new ThreadLocal<>();

  @VisibleForTesting protected static final long DEFAULT_CLOCK_TICKS_PER_SECOND = 100;

  public CpuMetricsCollector() {}

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(CpuMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");
    String procFileContents = readProcFile();
    String[] fields =
        procFileContents != null
            ? procFileContents.split(" ", PROC_CHILD_SYSTEM_TIME_FIELD + 2)
            : null;

    if (fields == null || fields.length < PROC_CHILD_SYSTEM_TIME_FIELD + 1) {
      return false;
    }

    try {
      snapshot.userTimeS = readFieldAsS(fields[PROC_USER_TIME_FIELD]);
      snapshot.systemTimeS = readFieldAsS(fields[PROC_SYSTEM_TIME_FIELD]);
      snapshot.childUserTimeS = readFieldAsS(fields[PROC_CHILD_USER_TIME_FIELD]);
      snapshot.childSystemTimeS = readFieldAsS(fields[PROC_CHILD_SYSTEM_TIME_FIELD]);
    } catch (NumberFormatException nfe) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse CPU time field", nfe);
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

  @VisibleForTesting
  @Nullable
  protected String readProcFile() {
    StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
    RandomAccessFile procFile = null;
    try {
      procFile = new RandomAccessFile(PROC_STAT_FILE_PATH, "r");
      return procFile.readLine();
    } catch (IOException ioe) {
      return null;
    } finally {
      if (procFile != null) {
        try {
          procFile.close();
        } catch (IOException ignored) {
          // ignored
        }
      }
      StrictMode.setThreadPolicy(originalPolicy);
    }
  }

  private static double readFieldAsS(String field) throws NumberFormatException {
    return Long.parseLong(field) * 1.0 / getClockTicksPerSecond();
  }

  static long getClockTicksPerSecond() {
    return Initializer.CLOCK_TICKS_PER_SECOND;
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
