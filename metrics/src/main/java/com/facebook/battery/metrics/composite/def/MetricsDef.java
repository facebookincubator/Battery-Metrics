/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.composite.def;

import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;

/**
 * Class that binds a {@link SystemMetrics} class to its respective driver classes. See {@link
 * CompositeMetricsHolder}.
 */
public class MetricsDef<T extends SystemMetrics<T>> {
  public final Class<T> clazz;
  public final SystemMetricsCollector<T> collector;
  public final SystemMetricsReporter<T> reporter;
  public final SystemMetricsSerializer<T> serializer;
  public final boolean isNonZeroAtColdStart;

  public MetricsDef(
      Class<T> clazz,
      SystemMetricsCollector<T> collector,
      SystemMetricsReporter<T> reporter,
      SystemMetricsSerializer<T> serializer,
      boolean isNonZeroAtColdStart) {
    this.clazz = clazz;
    this.collector = collector;
    this.reporter = reporter;
    this.serializer = serializer;
    this.isNonZeroAtColdStart = isNonZeroAtColdStart;
  }
}
