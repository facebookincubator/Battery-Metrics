/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.composite;

import com.facebook.battery.metrics.core.SystemMetrics;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a set of metrics internally that can be simply iterated over and used by being wrapped
 * with this class.
 */
public class CompositeMetrics extends SystemMetrics<CompositeMetrics> {

  private static final long serialVersionUID = 0;

  private final Map<Class<? extends SystemMetrics>, SystemMetrics> mMetricsMap = new HashMap<>();

  public CompositeMetrics() {}

  @Override
  public CompositeMetrics diff(CompositeMetrics b, CompositeMetrics result) {
    for (Class<? extends SystemMetrics> c : mMetricsMap.keySet()) {
      getMetric(c).diff(b.getMetric(c), result.getMetric(c));
    }
    return result;
  }

  @Override
  public CompositeMetrics sum(CompositeMetrics b, CompositeMetrics result) {
    for (Class<? extends SystemMetrics> c : mMetricsMap.keySet()) {
      getMetric(c).sum(b.getMetric(c), result.getMetric(c));
    }
    return result;
  }

  @Override
  public CompositeMetrics set(CompositeMetrics b) {
    for (Class<? extends SystemMetrics> c : mMetricsMap.keySet()) {
      getMetric(c).set(b.getMetric(c));
    }
    return this;
  }

  public <T extends SystemMetrics<T>> CompositeMetrics putMetric(Class<T> metricsClass, T metric) {
    mMetricsMap.put(metricsClass, metric);
    return this;
  }

  public <T extends SystemMetrics<T>> T getMetric(Class<T> metricsClass) {
    return metricsClass.cast(mMetricsMap.get(metricsClass));
  }

  public Map<Class<? extends SystemMetrics>, SystemMetrics> getMetrics() {
    return mMetricsMap;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("Composite Metrics:\n");
    for (Class<? extends SystemMetrics> c : mMetricsMap.keySet()) {
      SystemMetrics m = getMetric(c);
      b.append(m.toString());
      b.append("\n");
    }
    return b.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CompositeMetrics that = (CompositeMetrics) o;

    return mMetricsMap.equals(that.mMetricsMap);
  }

  @Override
  public int hashCode() {
    return mMetricsMap.hashCode();
  }
}
