/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.composite

import androidx.collection.SimpleArrayMap
import com.facebook.battery.metrics.composite.CompositeMetrics
import com.facebook.battery.metrics.core.SystemMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class CompositeMetricsReporter() : SystemMetricsReporter<CompositeMetrics> {

  private val metricsReporterMap =
      SimpleArrayMap<Class<out SystemMetrics<*>?>, SystemMetricsReporter<*>>()

  override fun reportTo(metrics: CompositeMetrics, event: SystemMetricsReporter.Event) {
    for (i in 0..<metricsReporterMap.size()) {
      val metricsClass = metricsReporterMap.keyAt(i)
      if (metrics.isValid(metricsClass)) {
        val systemMetrics = metrics.getMetric(metricsClass)
        val reporter = metricsReporterMap[metricsClass]
        @Suppress(
            "UNCHECKED_CAST",
            "UPPER_BOUND_VIOLATED_IN_TYPE_OPERATOR_OR_PARAMETER_BOUNDS_WARNING",
        )
        // Heterogeneous map keyed by Class; cast sound by addMetricsReporter contract (KTLC-358:
        // star cannot satisfy F-bound; real fix is 2.4 migration)
        (checkNotNull(reporter) as SystemMetricsReporter<SystemMetrics<*>>).reportTo(
            systemMetrics,
            event,
        )
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
  fun <T : SystemMetrics<T>?> addMetricsReporter(
      metricsClass: Class<out T>,
      reporter: SystemMetricsReporter<out T>,
  ): CompositeMetricsReporter {
    metricsReporterMap.put(metricsClass, reporter)
    return this
  }

  @Suppress("UNCHECKED_CAST") // heterogeneous map read (input contract)
  fun <S : SystemMetrics<S>?, T : SystemMetricsReporter<S>?> getReporter(
      metricsClass: Class<S>,
  ): T? = metricsReporterMap[metricsClass] as T
}
