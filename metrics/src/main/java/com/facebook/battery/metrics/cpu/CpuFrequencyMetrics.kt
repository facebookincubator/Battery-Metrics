/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu

import android.util.SparseIntArray
import com.facebook.battery.metrics.core.SystemMetrics
import com.facebook.battery.metrics.core.SystemMetricsLogger
import org.json.JSONException
import org.json.JSONObject

/**
 * Maintains the frequency each core was running, with a sparse int array mapping frequencies to
 * time running at that frequency per core on phone.
 *
 * To keep things simple, the number of cores is determined statically and fixed on the device so
 * that it can't change or cause unexpected bugs - see
 * [CpuFrequencyMetricsCollector#getTotalCores()].
 */
class CpuFrequencyMetrics : SystemMetrics<CpuFrequencyMetrics>() {

  @JvmField val timeInStateS: Array<SparseIntArray>

  init {
    val cores = CpuFrequencyMetricsCollector.totalCores
    this.timeInStateS = Array(cores) { SparseIntArray(0) }
  }

  override fun sum(
      b: CpuFrequencyMetrics?,
      output: CpuFrequencyMetrics?,
  ): CpuFrequencyMetrics {
    var output = output
    if (output == null) {
      output = CpuFrequencyMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      for (i in timeInStateS.indices) {
        val aCore = timeInStateS[i]
        val bCore = b.timeInStateS[i]
        val outputCore = output.timeInStateS[i]

        for (j in 0..<aCore.size()) {
          val frequency = aCore.keyAt(j)
          outputCore.put(frequency, aCore.valueAt(j) + bCore[frequency, 0])
        }

        for (j in 0..<bCore.size()) {
          val frequency = bCore.keyAt(j)
          if (aCore.indexOfKey(frequency) < 0) {
            outputCore.put(frequency, bCore.valueAt(j))
          }
        }
      }
    }

    return output
  }

  /**
   * Subtracts b from the current value while being aware of core restarts.
   *
   * Cpu clusters can be switched on and off as required by some devices: in those cases, the
   * measured frequency can go *down* across snapshots legally.
   *
   * If the time in state for any core appears to have reduced, we can infer that the core was
   * switched off and restarted. In that case, a better approximation is the current value of the
   * snapshot instead of a meaningless subtraction.
   *
   * Some tests make this behavior more explicit: {@see CpuFrequencyMetricsTest#testDiff} and {@see
   * CpuFrequencyMetricsTest#testDiffWithCoreReset} for expected behavior.
   */
  override fun diff(
      b: CpuFrequencyMetrics?,
      output: CpuFrequencyMetrics?,
  ): CpuFrequencyMetrics {
    var output = output
    if (output == null) {
      output = CpuFrequencyMetrics()
    }

    if (b == null) {
      output.set(this)
    } else {
      for (i in timeInStateS.indices) {
        val aCore = timeInStateS[i]
        val bCore = b.timeInStateS[i]
        val outputCore = output.timeInStateS[i]

        var hasCoreReset = false
        var j = 0
        val size = aCore.size()
        while (j < size && !hasCoreReset) {
          val frequency = aCore.keyAt(j)
          val difference = aCore.valueAt(j) - bCore[frequency, 0]

          if (difference < 0) {
            hasCoreReset = true
            break
          }
          outputCore.put(frequency, difference)
          j++
        }

        if (hasCoreReset) {
          copyArrayInto(aCore, outputCore)
        }
      }
    }

    return output
  }

  override fun set(b: CpuFrequencyMetrics): CpuFrequencyMetrics {
    for (i in timeInStateS.indices) {
      copyArrayInto(b.timeInStateS[i], timeInStateS[i])
    }

    return this
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) {
      return true
    }
    if (o == null || javaClass != o.javaClass) {
      return false
    }

    val that = o as CpuFrequencyMetrics
    if (timeInStateS.size != that.timeInStateS.size) {
      return false
    }

    var i = 0
    val size = timeInStateS.size
    while (i < size) {
      if (!sparseIntArrayEquals(timeInStateS[i], that.timeInStateS[i])) {
        return false
      }
      i++
    }

    return true
  }

  /** Based off [AbstractMap#hashCode()]: returns the sum of the hashcodes of the entries. */
  override fun hashCode(): Int {
    var hash = 0
    for (i in timeInStateS.indices) {
      val array = timeInStateS[i]
      var j = 0
      val size = timeInStateS[i].size()
      while (j < size) {
        // hash of an integer is the integer itself - see [Integers#hashCode]
        hash += array.keyAt(j) xor array.valueAt(j)
        j++
      }
    }
    return hash
  }

  override fun toString(): String =
      "CpuFrequencyMetrics{timeInStateS=${timeInStateS.contentToString()}${'}'}"

  fun toJSONObject(): JSONObject? {
    if (timeInStateS.size == 0) {
      return null
    }

    // This is slightly more complex than simply using a hashmap to aggregate frequencies
    // because SparseIntArray doesn't override equals/hash correctly.
    // Implemented in a fairly expensive, n^2 way because number of cores is presumably
    // very low.
    val isHandled = BooleanArray(timeInStateS.size)
    val output = JSONObject()
    var i = 0
    val cores = timeInStateS.size
    while (i < cores) {
      val current = timeInStateS[i]
      if (current.size() == 0 || isHandled[i]) {
        i++
        continue
      }

      var cpumask = 1 shl i

      for (j in i + 1..<cores) {
        if (sparseIntArrayEquals(current, timeInStateS[j])) {
          cpumask = cpumask or (1 shl j)
          isHandled[j] = true
        }
      }

      try {
        output.put(Integer.toHexString(cpumask), convert(current))
      } catch (je: JSONException) {
        SystemMetricsLogger.wtf("CpuFrequencyMetricsReporter", "Unable to store event", je)
      }
      i++
    }

    return output
  }

  companion object {
    private fun copyArrayInto(
        source: SparseIntArray,
        destination: SparseIntArray,
    ) {
      destination.clear()
      for (i in 0..<source.size()) {
        destination.append(source.keyAt(i), source.valueAt(i))
      }
    }

    /**
     * Based off [java.util.AbstractMap#equals] -- with simplifications because we're guaranteed
     * sparse int arrays with no nullable values or casts.
     *
     * TODO Make this a utility method, along with hashcode.
     */
    private fun sparseIntArrayEquals(a: SparseIntArray, b: SparseIntArray): Boolean {
      if (a === b) {
        return true
      }

      val aSize = a.size()
      if (aSize != b.size()) {
        return false
      }

      // Sparse int arrays keep a sorted list of values: which means equality can just walk through
      // both arrays to check.
      return (0..<aSize).none { i -> a.keyAt(i) != b.keyAt(i) || a.valueAt(i) != b.valueAt(i) }
    }

    @Throws(JSONException::class)
    private fun convert(array: SparseIntArray): JSONObject {
      val result = JSONObject()
      var j = 0
      val frequencies = array.size()
      while (j < frequencies) {
        result.put(array.keyAt(j).toString(), array.valueAt(j))
        j++
      }
      return result
    }
  }
}
