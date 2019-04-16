/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.cpu;

import static com.facebook.battery.metrics.core.Utilities.checkNotNull;

import android.util.SparseIntArray;
import androidx.annotation.VisibleForTesting;
import com.facebook.battery.metrics.core.ProcFileReader;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.VisibleToAvoidSynthetics;
import com.facebook.infer.annotation.ThreadSafe;
import java.io.File;
import java.io.FilenameFilter;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * Capture CPU frequency statistics.
 *
 * <p>Note that cpu frequencies are device wide and not specific to a process: this will be more
 * accurate the more frequently it's sampled, but there are times when it will mis-attribute
 * different frequencies while the instrumented app is inactive. The net cpu time of this app at
 * those times should also be very low, so the net energy impact should not be too affected.
 *
 * <p>The available statistics are well documented on kernel.org at
 * https://www.kernel.org/doc/Documentation/cpu-freq/cpufreq-stats.txt.
 */
@ThreadSafe
public class CpuFrequencyMetricsCollector extends SystemMetricsCollector<CpuFrequencyMetrics> {

  private static final String CPU_DATA_PATH = "/sys/devices/system/cpu/";
  @VisibleToAvoidSynthetics static int sCoresForTest = -1;

  @GuardedBy("this")
  @Nullable
  private ProcFileReader[] mFiles;

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(CpuFrequencyMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");
    boolean hasAnyValid = false;
    for (int i = 0, cores = getTotalCores(); i < cores; i++) {
      hasAnyValid |= readCoreStats(snapshot.timeInStateS[i], getReader(i));
    }

    return hasAnyValid;
  }

  @VisibleForTesting
  protected String getPath(int core) {
    return CPU_DATA_PATH + "cpu" + core + "/cpufreq/stats/time_in_state";
  }

  private synchronized ProcFileReader getReader(int core) {
    if (mFiles == null) {
      mFiles = new ProcFileReader[getTotalCores()];
    }

    if (mFiles[core] == null) {
      mFiles[core] = new ProcFileReader(getPath(core)).start();
    } else {
      mFiles[core].reset();
    }

    return mFiles[core];
  }

  private synchronized boolean readCoreStats(SparseIntArray array, ProcFileReader reader) {
    array.clear();

    // A failure is mostly expected because files become inaccessible in case of
    // the core being taken offline.
    if (!reader.isValid()) {
      return false;
    }

    try {
      while (reader.hasNext()) {
        long frequency = reader.readNumber();
        reader.skipSpaces();
        long timeInState = reader.readNumber() / CpuMetricsCollector.getClockTicksPerSecond();
        reader.skipLine();

        array.put((int) frequency, (int) timeInState);
      }
    } catch (ProcFileReader.ParseException pe) {
      return false;
    }

    return true;
  }

  @Override
  public CpuFrequencyMetrics createMetrics() {
    return new CpuFrequencyMetrics();
  }

  /**
   * Returns total cores available on the system: note that this is different from {@link
   * Runtime#availableProcessors()} which will exclude currently offline processors.
   */
  public static int getTotalCores() {
    return Initializer.CORES;
  }

  /** Override cores: this only works /before/ the first call to getTotalCores. */
  @VisibleForTesting
  public static void overrideCores() {
    sCoresForTest = 4;
    if (getTotalCores() != sCoresForTest) {
      throw new RuntimeException(
          "Unable to override cores! Has getTotalCores() already been called?");
    }
  }

  private static class Initializer {
    public static final int CORES;

    static {
      int configuredProcessors;
      if (sCoresForTest > 0) {
        configuredProcessors = sCoresForTest;
      } else {
        configuredProcessors = (int) Sysconf.getScNProcessorsConf(-1);
        if (configuredProcessors < 0) {
          configuredProcessors = getProcessorCountFromProc();
        }
      }
      CORES = configuredProcessors;
    }

    private static int getProcessorCountFromProc() {
      File cpuData = new File(CPU_DATA_PATH);
      if (!cpuData.exists() || !cpuData.isDirectory()) {
        return 0;
      }

      File[] cpuFiles =
          cpuData.listFiles(
              new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                  return name.matches("cpu\\d+");
                }
              });
      return cpuFiles.length;
    }
  }
}
