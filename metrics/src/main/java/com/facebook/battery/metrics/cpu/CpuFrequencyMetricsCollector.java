// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.cpu;

import android.support.annotation.VisibleForTesting;
import android.util.SparseIntArray;
import com.facebook.battery.metrics.api.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;
import java.io.File;
import java.io.FilenameFilter;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * Capture CPU frequency statistics.
 *
 * <p>Note that cpu frequencies are device wide and not specific to a process.
 *
 * <p>The available statistics are well documented on kernel.org at
 * https://www.kernel.org/doc/Documentation/cpu-freq/cpufreq-stats.txt.
 */
@ThreadSafe
public class CpuFrequencyMetricsCollector extends SystemMetricsCollector<CpuFrequencyMetrics> {

  private static final String CPU_DATA_PATH = "/sys/devices/system/cpu/";
  private static int sCoresForTest = -1;

  @GuardedBy("this")
  @Nullable
  private ProcFileReader[] mFiles;

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(CpuFrequencyMetrics snapshot) {
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
    }

    return mFiles[core];
  }

  private static synchronized boolean readCoreStats(SparseIntArray array, ProcFileReader reader) {
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
        reader.skipLines();

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
