/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.wakelock;

import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.wakelock.WakeLockMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.infer.annotation.Nullsafe;
import org.json.JSONException;
import org.json.JSONObject;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class WakeLockMetricsReporter implements SystemMetricsReporter<WakeLockMetrics> {

  public static final String TAG = WakeLockMetricsReporter.class.getSimpleName();

  public static final String HELD_TIME_MS = "wakelock_held_time_ms";
  public static final String TAG_TIME_MS = "wakelock_tag_time_ms";
  public static final String ACQUIRED_COUNT = "wakelock_acquired_count";

  private boolean mShouldReportAttribution = true;

  @Override
  public void reportTo(WakeLockMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.heldTimeMs != 0) {
      event.add(HELD_TIME_MS, metrics.heldTimeMs);
    }

    if (metrics.acquiredCount != 0) {
      event.add(ACQUIRED_COUNT, metrics.acquiredCount);
    }

    if (mShouldReportAttribution) {
      try {
        JSONObject tagAttribution = metrics.attributionToJSONObject();
        if (tagAttribution != null) {
          event.add(TAG_TIME_MS, tagAttribution.toString());
        }
      } catch (JSONException ex) {
        SystemMetricsLogger.wtf(TAG, "Failed to serialize wakelock attribution data", ex);
      }
    }
  }

  /** Allows selecting if attribution should be included in the logged event. */
  public WakeLockMetricsReporter setShouldReportAttribution(boolean enabled) {
    mShouldReportAttribution = enabled;
    return this;
  }
}
