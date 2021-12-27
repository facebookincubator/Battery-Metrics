/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.composite;

import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;

public class CompositeMetricsReporter implements SystemMetricsReporter<CompositeMetrics> {

  private final SimpleArrayMap<Class<? extends SystemMetrics>, SystemMetricsReporter<?>>
      mMetricsReporterMap = new SimpleArrayMap<>();

  public void reportTo(CompositeMetrics metrics, SystemMetricsReporter.Event event) {
    for (int i = 0; i < mMetricsReporterMap.size(); i++) {
      Class<? extends SystemMetrics> metricsClass = mMetricsReporterMap.keyAt(i);
      if (metrics.isValid(metricsClass)) {
        SystemMetrics systemMetrics = metrics.getMetric(metricsClass);
        SystemMetricsReporter reporter = mMetricsReporterMap.get(metricsClass);
        reporter.reportTo(systemMetrics, event);
      }
    }
  }

  /**
   * Add a metric and its reporter to the CompositeMetricsReporter.
   *
   * @param metricsClass Class of the metric
   * @param reporter Reporter that reports the metrics class
   * @return Instance of this CompositeMetricsReporter
   */
  public <T extends SystemMetrics<T>> CompositeMetricsReporter addMetricsReporter(
      Class<T> metricsClass, SystemMetricsReporter<T> reporter) {
    mMetricsReporterMap.put(metricsClass, reporter);
    return this;
  }

  @Nullable
  public <S extends SystemMetrics<S>, T extends SystemMetricsReporter<S>> T getReporter(
      Class<S> metricsClass) {
    return (T) mMetricsReporterMap.get(metricsClass);
  }
}
