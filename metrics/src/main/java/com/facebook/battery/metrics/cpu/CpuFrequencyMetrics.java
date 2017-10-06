// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.cpu;

import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import com.facebook.battery.metrics.api.SystemMetrics;
import java.util.AbstractMap;
import java.util.Arrays;

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
  public boolean equals(Object o) {
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
}
