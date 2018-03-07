// Copyright 2004-present Facebook. All Rights Reserved.

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
