/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.composite.def

import com.facebook.battery.metrics.composite.CompositeMetrics
import com.facebook.battery.metrics.composite.CompositeMetricsCollector
import com.facebook.battery.metrics.core.SystemMetrics
import com.facebook.battery.metrics.core.SystemMetricsCollector
import com.facebook.battery.reporter.composite.CompositeMetricsReporter
import com.facebook.battery.serializer.composite.CompositeMetricsSerializer

class CompositeMetricsHolder
private constructor(
    @JvmField val collector: CompositeMetricsCollector,
    @JvmField val reporter: CompositeMetricsReporter,
    @JvmField val serializer: CompositeMetricsSerializer,
    private val nonZeroAtColdStartDefs: List<MetricsDef<*>>,
) {

  fun createInitialMetrics(): CompositeMetrics {
    val initialMetrics = collector.createMetrics()
    val metrics = initialMetrics.metrics
    var i = 0
    val size = metrics.size()
    while (i < size) {
      initialMetrics.setIsValid(metrics.keyAt(i), true)
      i++
    }

    for (metricsDef in nonZeroAtColdStartDefs) {
      takeInitialSnapshot(initialMetrics, metricsDef)
    }
    return initialMetrics
  }

  private fun <T : SystemMetrics<T>?> takeInitialSnapshot(
      initialMetrics: CompositeMetrics,
      metricsDef: MetricsDef<T>,
  ) {
    val metricsCollector: SystemMetricsCollector<T>? =
        collector.getMetricsCollector<T, SystemMetricsCollector<T>>(metricsDef.clazz)
    if (metricsCollector != null) {
      metricsCollector.getSnapshot(initialMetrics.getMetric(metricsDef.clazz))
    }
  }

  companion object {
    @JvmStatic
    fun create(
        metricsDefs: Iterable<MetricsDef<out SystemMetrics<*>?>>,
    ): CompositeMetricsHolder {
      val collectorBuilder = CompositeMetricsCollector.Builder()
      val reporter = CompositeMetricsReporter()
      val serializer = CompositeMetricsSerializer()
      val nonZeroAtColdStartDefs = ArrayList<MetricsDef<*>>()
      for (metrics in metricsDefs) {
        addMetricsDef(metrics, collectorBuilder, reporter, serializer)
        if (metrics.isNonZeroAtColdStart) {
          nonZeroAtColdStartDefs.add(metrics)
        }
      }
      return CompositeMetricsHolder(
          collectorBuilder.build(),
          reporter,
          serializer,
          nonZeroAtColdStartDefs,
      )
    }

    private fun <T : SystemMetrics<T>?> addMetricsDef(
        metrics: MetricsDef<T>,
        collectorBuilder: CompositeMetricsCollector.Builder,
        reporter: CompositeMetricsReporter,
        serializer: CompositeMetricsSerializer,
    ) {
      collectorBuilder.addMetricsCollector(metrics.clazz, metrics.collector)
      reporter.addMetricsReporter(metrics.clazz, metrics.reporter)
      serializer.addMetricsSerializer(metrics.clazz, metrics.serializer)
    }
  }
}
