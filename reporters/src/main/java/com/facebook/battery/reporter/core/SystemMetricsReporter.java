/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.core;

import android.support.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;

/**
 * SystemMetricsReporters attempt to standardize the values logged by different metrics objects
 * while being agnostic of the underlying analytics system.
 *
 * <p>Wrap the analytics library with the Event class that can create and log events, and then
 * simply call the appropriate {@link #reportTo(SystemMetrics, Event)} with a metrics object to
 * trigger an event.
 */
public interface SystemMetricsReporter<T extends SystemMetrics<T>> {

  void reportTo(T metrics, SystemMetricsReporter.Event event);

  interface Event {
    boolean isSampled();

    void acquireEvent(@Nullable String moduleName, String eventName);

    void add(String key, String value);

    void add(String key, int value);

    void add(String key, long value);

    void add(String key, double value);

    void logAndRelease();
  }
}
