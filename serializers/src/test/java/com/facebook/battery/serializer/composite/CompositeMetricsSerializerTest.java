/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.composite;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.time.TimeMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import com.facebook.battery.serializer.time.TimeMetricsSerializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CompositeMetricsSerializerTest extends SystemMetricsSerializerTest<CompositeMetrics> {

  @Test
  public void testInvalidInnerMetrics() throws Exception {
    CompositeMetrics instance = createInitializedInstance();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getSerializer().serialize(instance, new DataOutputStream(baos));

    byte[] byteArray = baos.toByteArray();
    byteArray[21] = (byte) (byteArray[13] + 1); // break the tag for TimeMetrics

    CompositeMetrics metrics = createInstance();
    assertThat(getSerializer()
        .deserialize(metrics, new DataInputStream(new ByteArrayInputStream(byteArray)))).isFalse();
  }

  @Test
  public void testInvalidMetricsSkipped() throws Exception {
    CompositeMetrics instance = createInstance();
    instance.setIsValid(TimeMetrics.class, false);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getSerializer().serialize(instance, new DataOutputStream(baos));

    byte[] byteArray = baos.toByteArray();

    CompositeMetrics metrics = createInstance();
    assertThat(
            getSerializer()
                .deserialize(metrics, new DataInputStream(new ByteArrayInputStream(byteArray))))
        .isTrue();
    assertThat(metrics.isValid(TimeMetrics.class)).isFalse();
  }

  @Override
  protected Class<CompositeMetrics> getClazz() {
    return CompositeMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<CompositeMetrics> getSerializer() {
    return new CompositeMetricsSerializer()
        .addMetricsSerializer(TimeMetrics.class, new TimeMetricsSerializer());
  }

  @Override
  protected CompositeMetrics createInitializedInstance() throws Exception {
    CompositeMetrics metrics = createInstance();
    metrics.getMetric(TimeMetrics.class).uptimeMs = 1;
    metrics.getMetric(TimeMetrics.class).realtimeMs = 2;
    metrics.setIsValid(TimeMetrics.class, true);
    return metrics;
  }

  @Override
  protected CompositeMetrics createInstance() throws Exception {
    return new CompositeMetrics().putMetric(TimeMetrics.class, new TimeMetrics());
  }
}
