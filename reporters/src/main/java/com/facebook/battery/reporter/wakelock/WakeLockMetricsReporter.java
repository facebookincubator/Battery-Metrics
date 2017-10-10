/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.wakelock;

import com.facebook.battery.metrics.api.SystemMetricsLogger;
import com.facebook.battery.metrics.wakelock.WakeLockMetrics;
import com.facebook.battery.reporter.api.SystemMetricsReporter;
import org.json.JSONException;
import org.json.JSONObject;

public class WakeLockMetricsReporter implements SystemMetricsReporter<WakeLockMetrics> {

  public static final String HELD_TIME_MS = "wakelock_held_time_ms";
  public static final String TAG_TIME_MS = "wakelock_tag_time_ms";

  @Override
  public void reportTo(WakeLockMetrics metrics, Event event) {
    if (metrics.heldTimeMs != 0) {
      event.add(HELD_TIME_MS, metrics.heldTimeMs);
    }

    // Creates a JSON Blob as a string of nested values that we can then process on thes server.
    if (metrics.isAttributionEnabled) {
      try {
        JSONObject attribution = new JSONObject();
        for (int i = 0, size = metrics.tagTimeMs.size(); i < size; i++) {
          long tagTimeMs = metrics.tagTimeMs.valueAt(i);
          if (tagTimeMs > 0) {
            attribution.put(metrics.tagTimeMs.keyAt(i), tagTimeMs);
            event.add(TAG_TIME_MS, attribution.toString());
          }
        }
      } catch (JSONException ex) {
        SystemMetricsLogger.wtf(
            "WakeLockMetricsReporter", "Failed to serialize attribution data", ex);
      }
    }
  }
}
