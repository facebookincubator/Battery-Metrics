/*
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.healthstats;

import android.os.Build;
import android.support.annotation.RequiresApi;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.healthstats.HealthStatsMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import org.json.JSONException;

@RequiresApi(api = Build.VERSION_CODES.N)
public class HealthStatsMetricsReporter implements SystemMetricsReporter<HealthStatsMetrics> {

  private static final String HEALTHSTATS = "healthstats";

  @Override
  public void reportTo(HealthStatsMetrics metrics, SystemMetricsReporter.Event event) {
    try {
      event.add(HEALTHSTATS, metrics.toJSONObject().toString());
    } catch (JSONException jsone) {
      SystemMetricsLogger.wtf(
          "HealthStatsMetricsReporter", "Couldn't log healthstats metrics", jsone);
    }
  }
}
