/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.composite;

import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.Utilities;
import com.facebook.infer.annotation.Nullsafe;

/**
 * Maintains a set of metrics internally that can be simply iterated over and used by being wrapped
 * with this class.
 */
@Nullsafe(Nullsafe.Mode.LOCAL)
public class CompositeMetrics extends SystemMetrics<CompositeMetrics> {

  private final SimpleArrayMap<Class<? extends SystemMetrics>, SystemMetrics> mMetricsMap =
      new SimpleArrayMap<>();
  private final SimpleArrayMap<Class<? extends SystemMetrics>, Boolean> mMetricsValid =
      new SimpleArrayMap<>();

  @Override
  public CompositeMetrics diff(@Nullable CompositeMetrics b, @Nullable CompositeMetrics result) {
    if (result == null) {
      throw new IllegalArgumentException("CompositeMetrics doesn't support nullable results");
    }

    if (b == null) {
      result.set(this);
    } else {
      for (int i = 0, size = mMetricsMap.size(); i < size; i++) {
        Class c = mMetricsMap.keyAt(i);
        boolean valid = isValid(c) && b.isValid(c);

        if (valid) {
          SystemMetrics resultMetric = result.getMetric(c);
          if (resultMetric != null) {
            getMetric(c).diff(b.getMetric(c), resultMetric);
          }
        }
        result.setIsValid(c, valid);
      }
    }
    return result;
  }

  /**
   * Add the metrics from the given input metric set and store the result in the output object.
   * Invalid metrics are treated as zeros. The output metric will be invalid only if both input
   * metrics are invalid.
   */
  @Override
  public CompositeMetrics sum(@Nullable CompositeMetrics b, @Nullable CompositeMetrics result) {
    if (result == null) {
      throw new IllegalArgumentException("CompositeMetrics doesn't support nullable results");
    }

    if (b == null) {
      result.set(this);
    } else {
      for (int i = 0, size = mMetricsMap.size(); i < size; i++) {
        Class c = mMetricsMap.keyAt(i);
        boolean valid = true;

        if (isValid(c) && b.isValid(c)) {
          SystemMetrics resultMetric = result.getMetric(c);
          if (resultMetric != null) {
            getMetric(c).sum(b.getMetric(c), resultMetric);
          }
        } else if (isValid(c)) {
          result.getMetric(c).set(getMetric(c));
        } else if (b.isValid(c)) {
          result.getMetric(c).set(b.getMetric(c));
        } else {
          valid = false;
        }
        result.setIsValid(c, valid);
      }
    }
    return result;
  }

  @Override
  public CompositeMetrics set(CompositeMetrics input) {
    for (int i = 0, size = mMetricsMap.size(); i < size; i++) {
      Class c = mMetricsMap.keyAt(i);
      SystemMetrics metric = input.getMetric(c);
      if (metric != null) {
        getMetric(c).set(metric);
        setIsValid(c, input.isValid(c));
      } else {
        setIsValid(c, false);
      }
    }
    return this;
  }

  public <T extends SystemMetrics<T>> CompositeMetrics putMetric(Class<T> metricsClass, T metric) {
    mMetricsMap.put(metricsClass, metric);
    mMetricsValid.put(metricsClass, Boolean.FALSE);
    return this;
  }

  public <T extends SystemMetrics<T>> CompositeMetrics putValidMetric(
      Class<T> metricsClass, T metric) {
    mMetricsMap.put(metricsClass, metric);
    mMetricsValid.put(metricsClass, Boolean.TRUE);
    return this;
  }

  public <T extends SystemMetrics<T>> T getMetric(Class<T> metricsClass) {
    return metricsClass.cast(mMetricsMap.get(metricsClass));
  }

  /**
   * Indicates whether metric collection for a specific metric succeeded. This allows metric
   * collection and reporting to continue even if some of the metric collectors fail.
   */
  public boolean isValid(Class c) {
    Boolean value = mMetricsValid.get(c);
    return value != null && value;
  }

  public void setIsValid(Class c, boolean isValid) {
    mMetricsValid.put(c, isValid ? Boolean.TRUE : Boolean.FALSE);
  }

  public SimpleArrayMap<Class<? extends SystemMetrics>, SystemMetrics> getMetrics() {
    return mMetricsMap;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("Composite Metrics{\n");
    for (int i = 0, size = mMetricsMap.size(); i < size; i++) {
      b.append(mMetricsMap.valueAt(i))
          .append(isValid(mMetricsMap.keyAt(i)) ? " [valid]" : " [invalid]")
          .append('\n');
    }
    b.append("}");

    return b.toString();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CompositeMetrics that = (CompositeMetrics) o;

    return Utilities.simpleArrayMapEquals(mMetricsValid, that.mMetricsValid)
        && Utilities.simpleArrayMapEquals(mMetricsMap, that.mMetricsMap);
  }

  @Override
  public int hashCode() {
    int result = mMetricsMap.hashCode();
    result = 31 * result + mMetricsValid.hashCode();
    return result;
  }
}
