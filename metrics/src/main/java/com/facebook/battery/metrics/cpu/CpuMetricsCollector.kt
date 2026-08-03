/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu

import androidx.annotation.VisibleForTesting
import com.facebook.battery.metrics.core.ProcFileReader
import com.facebook.battery.metrics.core.SystemMetricsCollector
import com.facebook.battery.metrics.core.SystemMetricsLogger
import com.facebook.battery.metrics.core.Utilities
import com.facebook.battery.metrics.core.VisibleToAvoidSynthetics
import com.facebook.infer.annotation.ThreadSafe

/**
 * Collects data about cpu metrics.
 *
 * This data is read from `/proc/<pid>/stat` -- see the corresponding man page for details.
 */
@ThreadSafe
open class CpuMetricsCollector : SystemMetricsCollector<CpuMetrics>() {

  /**
   * Ensure that the cpu metrics value is always increasing: in case the cpu time captured goes
   * down, getSnapshot will return an error to prevent erroneous values. Having it be thread local
   * sidesteps several possible synchronization issues.
   */
  private val lastSnapshot = ThreadLocal<CpuMetrics>()

  private val procFileReader = ThreadLocal<ProcFileReader>()

  @ThreadSafe(enableChecks = false)
  override fun getSnapshot(snapshot: CpuMetrics): Boolean {
    Utilities.checkNotNull(snapshot, "Null value passed to getSnapshot!")

    try {
      var reader = procFileReader.get()
      if (reader == null) {
        reader = ProcFileReader(path!!)
        procFileReader.set(reader)
      }

      reader.reset()

      if (!reader.isValid) {
        return false
      }

      var index = 0
      while (index < PROC_USER_TIME_FIELD) {
        reader.skipSpaces()
        index++
      }

      snapshot.userTimeS = readField(reader)
      snapshot.systemTimeS = readField(reader)
      snapshot.childUserTimeS = readField(reader)
      snapshot.childSystemTimeS = readField(reader)
    } catch (pe: ProcFileReader.ParseException) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse CPU time field", pe)
      return false
    }

    var lastSnapshot = lastSnapshot.get()
    if (lastSnapshot == null) {
      lastSnapshot = CpuMetrics()
      this.lastSnapshot.set(lastSnapshot)
    }

    if (
        java.lang.Double.compare(
            snapshot.userTimeS,
            lastSnapshot.userTimeS,
        ) < 0 ||
            java.lang.Double.compare(
                snapshot.systemTimeS,
                lastSnapshot.systemTimeS,
            ) < 0 ||
            java.lang.Double.compare(
                snapshot.childUserTimeS,
                lastSnapshot.childUserTimeS,
            ) < 0 ||
            java.lang.Double.compare(
                snapshot.childSystemTimeS,
                lastSnapshot.childSystemTimeS,
            ) < 0
    ) {
      SystemMetricsLogger.wtf(
          TAG,
          "Cpu Time Decreased from $lastSnapshot to $snapshot",
      )
      return false
    }

    lastSnapshot.set(snapshot)
    return true
  }

  override fun createMetrics(): CpuMetrics = CpuMetrics()

  protected open val path: String?
    get() = PROC_STAT_FILE_PATH

  /**
   * Initialized on demand (https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom)
   * because Collectors can be called/created from any thread.
   */
  private object Initializer {
    @get:JvmStatic
    @VisibleToAvoidSynthetics
    val CLOCK_TICKS_PER_SECOND: Long = Sysconf.getScClkTck(DEFAULT_CLOCK_TICKS_PER_SECOND)
  }

  companion object {
    private const val TAG = "CpuMetricsCollector"
    private const val PROC_STAT_FILE_PATH = "/proc/self/stat"

    /** See http://man7.org/linux/man-pages/man5/proc.5.html for the indexes and description. */
    private const val PROC_USER_TIME_FIELD = 13

    @VisibleForTesting protected const val DEFAULT_CLOCK_TICKS_PER_SECOND: Long = 100L

    @get:JvmStatic
    val clockTicksPerSecond: Long
      get() = Initializer.CLOCK_TICKS_PER_SECOND

    private fun readField(reader: ProcFileReader): Double {
      val cpuTimeMs = reader.readNumber() * 1.0 / Initializer.CLOCK_TICKS_PER_SECOND
      reader.skipSpaces()
      return cpuTimeMs
    }
  }
}
