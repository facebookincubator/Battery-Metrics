/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.composite;

import android.support.v4.util.SimpleArrayMap;
import android.util.SparseArray;
import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CompositeMetricsSerializer extends SystemMetricsSerializer<CompositeMetrics> {

  private final SimpleArrayMap<Class<? extends SystemMetrics>, SystemMetricsSerializer<?>>
      mSerializers = new SimpleArrayMap<>();
  private final SparseArray<SystemMetricsSerializer<? extends SystemMetrics<?>>> mDeserializers =
      new SparseArray<>();
  private final SparseArray<Class<? extends SystemMetrics<?>>> mDeserializerClasses =
      new SparseArray<>();

  public <T extends SystemMetrics<T>> CompositeMetricsSerializer addMetricsSerializer(
      Class<T> metricsClass, SystemMetricsSerializer<T> serializer) {
    mSerializers.put(metricsClass, serializer);
    mDeserializers.put(serializer.getTag(), serializer);
    mDeserializerClasses.put(serializer.getTag(), metricsClass);
    return this;
  }

  @Override
  public void serializeContents(CompositeMetrics metrics, DataOutput output) throws IOException {
    int size = mSerializers.size();
    int validMetrics = 0;
    for (int i = 0; i < size; i++) {
      if (metrics.isValid(mSerializers.keyAt(i))) {
        validMetrics++;
      }
    }
    output.writeInt(validMetrics);
    for (int i = 0; i < size; i++) {
      Class metricsClass = mSerializers.keyAt(i);
      if (metrics.isValid(metricsClass)) {
        SystemMetricsSerializer serializer = mSerializers.valueAt(i);
        output.writeInt(serializer.getTag());
        serializer.serializeContents(metrics.getMetric(metricsClass), output);
      }
    }
  }

  @Override
  public boolean deserializeContents(CompositeMetrics metrics, DataInput input) throws IOException {
    // First, reset the metrics object to expect all invalid metrics
    SimpleArrayMap<Class<? extends SystemMetrics>, SystemMetrics> all = metrics.getMetrics();
    for (int i = 0, size = metrics.getMetrics().size(); i < size; i++) {
      metrics.setIsValid(all.keyAt(i), false);
    }

    int size = input.readInt();
    for (int i = 0; i < size; i++) {
      int tag = input.readInt();
      SystemMetricsSerializer deserializer = mDeserializers.get(tag);
      Class<? extends SystemMetrics> metricsClass = mDeserializerClasses.get(tag);
      if (deserializer == null || metricsClass == null) {
        return false;
      }

      SystemMetrics<? extends SystemMetrics<?>> metric = metrics.getMetric(metricsClass);
      if (!deserializer.deserializeContents(metric, input)) {
        return false;
      }

      metrics.setIsValid(metricsClass, true);
    }
    return true;
  }
}
