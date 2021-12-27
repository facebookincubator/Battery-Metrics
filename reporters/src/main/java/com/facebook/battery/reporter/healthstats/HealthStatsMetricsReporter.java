/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.healthstats;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.healthstats.HealthStatsMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.infer.annotation.Nullsafe;
import org.json.JSONException;

@Nullsafe(Nullsafe.Mode.LOCAL)
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
