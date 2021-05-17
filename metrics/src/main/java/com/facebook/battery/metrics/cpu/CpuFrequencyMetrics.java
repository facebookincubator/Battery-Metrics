/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu;

import android.util.SparseIntArray;
import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.infer.annotation.Nullsafe;
import java.util.AbstractMap;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Maintains the frequency each core was running, with a sparse int array mapping frequencies to
 * time running at that frequency per core on phone.
 *
 * <p>To keep things simple, the number of cores is determined statically and fixed on the device so
 * that it can't change or cause unexpected bugs - see {@link
 * CpuFrequencyMetricsCollector#getTotalCores()}.
 */
@Nullsafe(Nullsafe.Mode.LOCAL)
public class CpuFrequencyMetrics extends SystemMetrics<CpuFrequencyMetrics> {

  public final SparseIntArray[] timeInStateS;

  public CpuFrequencyMetrics() {
    int cores = CpuFrequencyMetricsCollector.getTotalCores();
    timeInStateS = new SparseIntArray[cores];
    for (int core = 0; core < cores; core++) {
      timeInStateS[core] = new SparseIntArray(0);
    }
  }

  @Override
  public CpuFrequencyMetrics sum(
      @Nullable CpuFrequencyMetrics b, @Nullable CpuFrequencyMetrics output) {
    if (output == null) {
      output = new CpuFrequencyMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      for (int i = 0; i < timeInStateS.length; i++) {
        SparseIntArray aCore = timeInStateS[i];
        SparseIntArray bCore = b.timeInStateS[i];
        SparseIntArray outputCore = output.timeInStateS[i];

        for (int j = 0; j < aCore.size(); j++) {
          int frequency = aCore.keyAt(j);
          outputCore.put(frequency, aCore.valueAt(j) + bCore.get(frequency, 0));
        }

        for (int j = 0; j < bCore.size(); j++) {
          int frequency = bCore.keyAt(j);
          if (aCore.indexOfKey(frequency) < 0) {
            outputCore.put(frequency, bCore.valueAt(j));
          }
        }
      }
    }

    return output;
  }

  /**
   * Subtracts b from the current value while being aware of core restarts.
   *
   * <p>Cpu clusters can be switched on and off as required by some devices: in those cases, the
   * measured frequency can go <em>down</em> across snapshots legally.
   *
   * <p>If the time in state for any core appears to have reduced, we can infer that the core was
   * switched off and restarted. In that case, a better approximation is the current value of the
   * snapshot instead of a meaningless subtraction.
   *
   * <p>Some tests make this behavior more explicit: {@see CpuFrequencyMetricsTest#testDiff} and
   * {@see CpuFrequencyMetricsTest#testDiffWithCoreReset} for expected behavior.
   */
  @Override
  public CpuFrequencyMetrics diff(
      @Nullable CpuFrequencyMetrics b, @Nullable CpuFrequencyMetrics output) {
    if (output == null) {
      output = new CpuFrequencyMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      for (int i = 0; i < timeInStateS.length; i++) {
        SparseIntArray aCore = timeInStateS[i];
        SparseIntArray bCore = b.timeInStateS[i];
        SparseIntArray outputCore = output.timeInStateS[i];

        boolean hasCoreReset = false;
        for (int j = 0, size = aCore.size(); j < size && !hasCoreReset; j++) {
          int frequency = aCore.keyAt(j);
          int difference = aCore.valueAt(j) - bCore.get(frequency, 0);

          if (difference < 0) {
            hasCoreReset = true;
            break;
          }
          outputCore.put(frequency, difference);
        }

        if (hasCoreReset) {
          copyArrayInto(aCore, outputCore);
        }
      }
    }

    return output;
  }

  @Override
  public CpuFrequencyMetrics set(CpuFrequencyMetrics b) {
    for (int i = 0; i < timeInStateS.length; i++) {
      copyArrayInto(b.timeInStateS[i], timeInStateS[i]);
    }

    return this;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CpuFrequencyMetrics that = (CpuFrequencyMetrics) o;
    if (timeInStateS.length != that.timeInStateS.length) {
      return false;
    }

    for (int i = 0, size = timeInStateS.length; i < size; i++) {
      if (!sparseIntArrayEquals(timeInStateS[i], that.timeInStateS[i])) {
        return false;
      }
    }

    return true;
  }

  private static void copyArrayInto(SparseIntArray source, SparseIntArray destination) {
    destination.clear();
    for (int i = 0; i < source.size(); i++) {
      destination.append(source.keyAt(i), source.valueAt(i));
    }
  }

  /**
   * Based off {@link java.util.AbstractMap#equals} -- with simplifications because we're guaranteed
   * sparse int arrays with no nullable values or casts.
   *
   * <p>TODO Make this a utility method, along with hashcode.
   */
  public static boolean sparseIntArrayEquals(SparseIntArray a, SparseIntArray b) {
    if (a == b) {
      return true;
    }

    int aSize = a.size();
    if (aSize != b.size()) {
      return false;
    }

    // Sparse int arrays keep a sorted list of values: which means equality can just walk through
    // both arrays to check.
    for (int i = 0; i < aSize; i++) {
      if (a.keyAt(i) != b.keyAt(i) || a.valueAt(i) != b.valueAt(i)) {
        return false;
      }
    }

    return true;
  }

  /** Based off {@link AbstractMap#hashCode()}: returns the sum of the hashcodes of the entries. */
  @Override
  public int hashCode() {
    int hash = 0;
    for (int i = 0; i < timeInStateS.length; i++) {
      SparseIntArray array = timeInStateS[i];
      for (int j = 0, size = timeInStateS[i].size(); j < size; j++) {
        // hash of an integer is the integer itself - see {@link Integers#hashCode}
        hash += array.keyAt(j) ^ array.valueAt(j);
      }
    }
    return hash;
  }

  @Override
  public String toString() {
    return "CpuFrequencyMetrics{" + "timeInStateS=" + Arrays.toString(timeInStateS) + '}';
  }

  public @Nullable JSONObject toJSONObject() {
    if (timeInStateS.length == 0) {
      return null;
    }

    // This is slightly more complex than simply using a hashmap to aggregate frequencies
    // because SparseIntArray doesn't override equals/hash correctly.
    // Implemented in a fairly expensive, n^2 way because number of cores is presumably
    // very low.
    boolean[] isHandled = new boolean[timeInStateS.length];
    JSONObject output = new JSONObject();
    for (int i = 0, cores = timeInStateS.length; i < cores; i++) {
      SparseIntArray current = timeInStateS[i];
      if (current.size() == 0 || isHandled[i]) {
        continue;
      }

      int cpumask = 1 << i;

      for (int j = i + 1; j < cores; j++) {
        if (CpuFrequencyMetrics.sparseIntArrayEquals(current, timeInStateS[j])) {
          cpumask |= 1 << j;
          isHandled[j] = true;
        }
      }

      try {
        output.put(Integer.toHexString(cpumask), convert(current));
      } catch (JSONException je) {
        SystemMetricsLogger.wtf("CpuFrequencyMetricsReporter", "Unable to store event", je);
      }
    }

    return output;
  }

  private static JSONObject convert(SparseIntArray array) throws JSONException {
    JSONObject result = new JSONObject();
    for (int j = 0, frequencies = array.size(); j < frequencies; j++) {
      result.put(Integer.toString(array.keyAt(j)), array.valueAt(j));
    }
    return result;
  }
}
