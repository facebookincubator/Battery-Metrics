/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.core;

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
