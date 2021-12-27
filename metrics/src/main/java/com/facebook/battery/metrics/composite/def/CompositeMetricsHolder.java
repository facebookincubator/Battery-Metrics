/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.composite.def;

import androidx.collection.SimpleArrayMap;
import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.composite.CompositeMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.reporter.composite.CompositeMetricsReporter;
import com.facebook.battery.serializer.composite.CompositeMetricsSerializer;
import java.util.ArrayList;
import java.util.List;

public class CompositeMetricsHolder {
  public final CompositeMetricsCollector collector;
  public final CompositeMetricsReporter reporter;
  public final CompositeMetricsSerializer serializer;
  private final List<MetricsDef> nonZeroAtColdStartDefs;

  public static CompositeMetricsHolder create(
      Iterable<MetricsDef<? extends SystemMetrics>> metricsDefs) {
    CompositeMetricsCollector.Builder collectorBuilder = new CompositeMetricsCollector.Builder();
    CompositeMetricsReporter reporter = new CompositeMetricsReporter();
    CompositeMetricsSerializer serializer = new CompositeMetricsSerializer();
    ArrayList<MetricsDef> nonZeroAtColdStartDefs = new ArrayList<>();
    for (MetricsDef metrics : metricsDefs) {
      collectorBuilder.addMetricsCollector(metrics.clazz, metrics.collector);
      reporter.addMetricsReporter(metrics.clazz, metrics.reporter);
      serializer.addMetricsSerializer(metrics.clazz, metrics.serializer);
      if (metrics.isNonZeroAtColdStart) {
        nonZeroAtColdStartDefs.add(metrics);
      }
    }
    return new CompositeMetricsHolder(
        collectorBuilder.build(), reporter, serializer, nonZeroAtColdStartDefs);
  }

  private CompositeMetricsHolder(
      CompositeMetricsCollector collector,
      CompositeMetricsReporter reporter,
      CompositeMetricsSerializer serializer,
      List<MetricsDef> nonZeroAtColdStartDefs) {
    this.collector = collector;
    this.reporter = reporter;
    this.serializer = serializer;
    this.nonZeroAtColdStartDefs = nonZeroAtColdStartDefs;
  }

  public CompositeMetrics createInitialMetrics() {
    CompositeMetrics initialMetrics = collector.createMetrics();
    SimpleArrayMap<Class<? extends SystemMetrics>, SystemMetrics> metrics =
        initialMetrics.getMetrics();
    for (int i = 0, size = metrics.size(); i < size; i++) {
      initialMetrics.setIsValid(metrics.keyAt(i), true);
    }

    for (MetricsDef metricsDef : nonZeroAtColdStartDefs) {
      SystemMetricsCollector metricsCollector = collector.getMetricsCollector(metricsDef.clazz);
      if (metricsCollector != null) {
        metricsCollector.getSnapshot(initialMetrics.getMetric(metricsDef.clazz));
      }
    }
    return initialMetrics;
  }
}
