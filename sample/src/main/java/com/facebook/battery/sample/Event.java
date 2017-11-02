/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.sample;

import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.battery.reporter.core.SystemMetricsReporter;

/** Poor man's analytics: also known as Logcat. */
public class Event implements SystemMetricsReporter.Event {
  @Override
  public boolean isSampled() {
    return true;
  }

  @Override
  public void acquireEvent(@Nullable String moduleName, String eventName) {
    Log.i("BatteryApplication", "New event: {");
  }

  @Override
  public void add(String key, String value) {
    Log.i("BatteryApplication", key + ":" + value);
  }

  @Override
  public void add(String key, int value) {
    Log.i("BatteryApplication", key + ":" + value);
  }

  @Override
  public void add(String key, double value) {
    Log.i("BatteryApplication", key + ":" + value);
  }

  @Override
  public void logAndRelease() {
    Log.i("BatteryApplication", "}");
  }
}
