/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu

import android.util.SparseIntArray
import androidx.annotation.VisibleForTesting
import com.facebook.battery.metrics.core.ProcFileReader
import com.facebook.battery.metrics.core.SystemMetricsCollector
import com.facebook.battery.metrics.core.Utilities
import com.facebook.battery.metrics.core.VisibleToAvoidSynthetics
import com.facebook.infer.annotation.ThreadSafe
import java.io.File
import java.io.FilenameFilter
import javax.annotation.concurrent.GuardedBy

/**
 * Capture CPU frequency statistics.
 *
 * Note that cpu frequencies are device wide and not specific to a process: this will be more
 * accurate the more frequently it's sampled, but there are times when it will mis-attribute
 * different frequencies while the instrumented app is inactive. The net cpu time of this app at
 * those times should also be very low, so the net energy impact should not be too affected.
 *
 * The available statistics are well documented on kernel.org at
 * https://www.kernel.org/doc/Documentation/cpu-freq/cpufreq-stats.txt.
 */
@ThreadSafe
open class CpuFrequencyMetricsCollector : SystemMetricsCollector<CpuFrequencyMetrics>() {

  @GuardedBy("this") private var files: Array<ProcFileReader?>? = null

  @ThreadSafe(enableChecks = false)
  override fun getSnapshot(snapshot: CpuFrequencyMetrics): Boolean {
    Utilities.checkNotNull(snapshot, "Null value passed to getSnapshot!")
    var hasAnyValid = false
    var i = 0
    val cores = totalCores
    while (i < cores) {
      hasAnyValid =
          hasAnyValid or
              readCoreStats(
                  snapshot.timeInStateS[i],
                  checkNotNull(getReader(i)),
              )
      i++
    }

    return hasAnyValid
  }

  @VisibleForTesting
  protected open fun getPath(core: Int): String? =
      "${CPU_DATA_PATH}cpu${core}/cpufreq/stats/time_in_state"

  @Synchronized
  private fun getReader(core: Int): ProcFileReader? {
    if (files == null) {
      files = arrayOfNulls(totalCores)
    }

    if (files!![core] == null) {
      files!![core] = ProcFileReader(getPath(core)!!).start()
    } else {
      files!![core]!!.reset()
    }

    return files!![core]
  }

  @Synchronized
  private fun readCoreStats(array: SparseIntArray, reader: ProcFileReader): Boolean {
    array.clear()

    // A failure is mostly expected because files become inaccessible in case of
    // the core being taken offline.
    if (!reader.isValid) {
      return false
    }

    try {
      while (reader.hasNext()) {
        val frequency = reader.readNumber()
        reader.skipSpaces()
        val timeInState = reader.readNumber() / CpuMetricsCollector.clockTicksPerSecond
        reader.skipLine()

        array.put(frequency.toInt(), timeInState.toInt())
      }
    } catch (pe: ProcFileReader.ParseException) {
      return false
    }

    return true
  }

  override fun createMetrics(): CpuFrequencyMetrics = CpuFrequencyMetrics()

  private object Initializer {
    val CORES: Int

    init {
      var configuredProcessors: Int
      if (sCoresForTest > 0) {
        configuredProcessors = sCoresForTest
      } else {
        configuredProcessors = Sysconf.scNProcessorsConf.toInt()
        if (configuredProcessors < 0) {
          configuredProcessors = processorCountFromProc
        }
      }
      CORES = configuredProcessors
    }

    @get:JvmStatic
    val processorCountFromProc: Int
      get() {
        val cpuData = File(CpuFrequencyMetricsCollector.CPU_DATA_PATH)
        if (!cpuData.exists() || !cpuData.isDirectory) {
          return 0
        }

        val cpuFiles =
            cpuData.listFiles(FilenameFilter { _, name -> name.matches("cpu\\d+".toRegex()) })
        if (cpuFiles != null) {
          return cpuFiles.size
        }
        return 0
      }
  }

  companion object {
    private const val CPU_DATA_PATH = "/sys/devices/system/cpu/"

    @JvmField @VisibleToAvoidSynthetics var sCoresForTest: Int = -1

    @get:JvmStatic
    val totalCores: Int
      /**
       * Returns total cores available on the system: note that this is different from
       * [Runtime#availableProcessors()] which will exclude currently offline processors.
       */
      get() = Initializer.CORES

    /** Override cores: this only works /before/ the first call to getTotalCores. */
    @JvmStatic
    @VisibleForTesting
    fun overrideCores() {
      sCoresForTest = 4
      if (totalCores != sCoresForTest) {
        throw RuntimeException("Unable to override cores! Has getTotalCores() already been called?")
      }
    }
  }
}
