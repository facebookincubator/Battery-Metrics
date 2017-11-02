/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.cpu;

import android.support.annotation.VisibleForTesting;
import android.util.SparseIntArray;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.cpu.CpuFrequencyMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Reports cpu statuses to an event. As a simplification, the CpuFrequencyMetricsCollector collects
 * and maintains information about each cpu instead of the cluster of cpus it belongs to because of
 * varying levels of topology file access observed across different phones.
 *
 * <p>The reporter groups equal frequency values together into a cpumask (similar to that reported
 * by the topology sysfs files) mapped to the corresponding frequency.
 */
public class CpuFrequencyMetricsReporter implements SystemMetricsReporter<CpuFrequencyMetrics> {

  @VisibleForTesting static final String CPU_TIME_IN_STATE_S = "cpu_time_in_state_s";

  @Override
  public void reportTo(CpuFrequencyMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.timeInStateS.length == 0) {
      return;
    }

    // This is slightly more complex than simply using a hashmap to aggregate frequencies
    // because SparseIntArray doesn't override equals/hash correctly.
    // Implemented in a fairly expensive, n^2 way because number of cores is presumably
    // very low.
    boolean[] isHandled = new boolean[metrics.timeInStateS.length];
    JSONObject output = new JSONObject();
    for (int i = 0, cores = metrics.timeInStateS.length; i < cores; i++) {
      SparseIntArray current = metrics.timeInStateS[i];
      if (current.size() == 0 || isHandled[i]) {
        continue;
      }

      int cpumask = 1 << i;

      for (int j = i + 1; j < cores; j++) {
        if (CpuFrequencyMetrics.sparseIntArrayEquals(current, metrics.timeInStateS[j])) {
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

    if (output.length() != 0) {
      event.add(CPU_TIME_IN_STATE_S, output.toString());
    }
  }

  private static JSONObject convert(SparseIntArray array) throws JSONException {
    JSONObject result = new JSONObject();
    for (int j = 0, frequencies = array.size(); j < frequencies; j++) {
      result.put(Integer.toString(array.keyAt(j)), array.valueAt(j));
    }
    return result;
  }
}
