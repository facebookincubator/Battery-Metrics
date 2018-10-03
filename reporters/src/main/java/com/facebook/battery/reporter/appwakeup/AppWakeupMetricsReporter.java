/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.appwakeup;

import com.facebook.battery.metrics.appwakeup.AppWakeupMetrics;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.reporter.core.SystemMetricsReporter;

import org.json.JSONArray;
import org.json.JSONException;

public class AppWakeupMetricsReporter implements SystemMetricsReporter<AppWakeupMetrics> {

  private static final String TAG = "AppWakeupMetricsReporter";

  private static final String APP_WAKEUPS = "app_wakeup_attribution";

  @Override
  public void reportTo(AppWakeupMetrics metrics, SystemMetricsReporter.Event event) {
    try {
      JSONArray representation = metrics.toJSON();
      if (representation != null) {
        event.add(APP_WAKEUPS, representation.toString());
      }
    } catch (JSONException jsone) {
      SystemMetricsLogger.wtf(TAG, "Unable to report AppWakeupMetrics", jsone);
    }
  }
}
