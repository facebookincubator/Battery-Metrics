/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.composite;

import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.Utilities;

/**
 * Maintains a set of metrics internally that can be simply iterated over and used by being wrapped
 * with this class.
 */
public class CompositeMetrics extends SystemMetrics<CompositeMetrics> {

  private final SimpleArrayMap<Class<? extends SystemMetrics>, SystemMetrics> mMetricsMap =
      new SimpleArrayMap<>();
  private final SimpleArrayMap<Class<? extends SystemMetrics>, Boolean> mMetricsValid =
      new SimpleArrayMap<>();

  @Override
  public CompositeMetrics diff(@Nullable CompositeMetrics b, CompositeMetrics result) {
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

  @Override
  public CompositeMetrics sum(@Nullable CompositeMetrics b, CompositeMetrics result) {
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
            getMetric(c).sum(b.getMetric(c), resultMetric);
          }
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

  public <T extends SystemMetrics<T>> T getMetric(Class<T> metricsClass) {
    return metricsClass.cast(mMetricsMap.get(metricsClass));
  }

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
  public boolean equals(Object o) {
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
