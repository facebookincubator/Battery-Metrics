/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.core;

import com.facebook.battery.reporter.core.SystemMetricsReporter;
import java.util.HashMap;
import javax.annotation.Nullable;

/**
 * This class implements SystemMetricsReporter.Event for testing purpose of all metrics reporters.
 */
public class ReporterEvent implements SystemMetricsReporter.Event {

  public HashMap<String, Object> eventMap = new HashMap<>();

  public ReporterEvent() {}

  @Override
  public void acquireEvent(@Nullable String moduleName, String eventName) {}

  @Override
  public void add(String key, String value) {
    eventMap.put(key, value);
  }

  @Override
  public void add(String key, int value) {
    eventMap.put(key, value);
  }

  @Override
  public void add(String key, long value) {
    eventMap.put(key, value);
  }

  @Override
  public void add(String key, double value) {
    eventMap.put(key, value);
  }

  @Override
  public boolean isSampled() {
    return true;
  }

  @Override
  public void logAndRelease() {}
}
