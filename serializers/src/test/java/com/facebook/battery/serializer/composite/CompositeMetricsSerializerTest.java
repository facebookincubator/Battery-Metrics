/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.serializer.composite;

import static org.assertj.core.api.Java6Assertions.assertThat;

import androidx.annotation.Nullable;
import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.time.TimeMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import com.facebook.battery.serializer.time.TimeMetricsSerializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
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
    assertThat(
            getSerializer()
                .deserialize(metrics, new DataInputStream(new ByteArrayInputStream(byteArray))))
        .isFalse();
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

  @Test(expected = RuntimeException.class)
  public void testMetricsWithTheSameTag() {
    CompositeMetricsSerializer serializer = new CompositeMetricsSerializer();
    serializer.addMetricsSerializer(A.class, new ASerializer());
    serializer.addMetricsSerializer(B.class, new BSerializer());
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

class A extends SystemMetrics<A> {

  @Override
  public A sum(@Nullable A b, @Nullable A output) {
    return null;
  }

  @Override
  public A diff(@Nullable A b, @Nullable A output) {
    return null;
  }

  @Override
  public A set(A b) {
    return null;
  }
}

class B extends SystemMetrics<B> {

  @Override
  public B sum(@Nullable B b, @Nullable B output) {
    return null;
  }

  @Override
  public B diff(@Nullable B b, @Nullable B output) {
    return null;
  }

  @Override
  public B set(B b) {
    return null;
  }
}

class ASerializer extends SystemMetricsSerializer<A> {

  @Override
  public long getTag() {
    return 1337;
  }

  @Override
  public void serializeContents(A metrics, DataOutput output) throws IOException {}

  @Override
  public boolean deserializeContents(A metrics, DataInput input) throws IOException {
    return false;
  }
}

class BSerializer extends SystemMetricsSerializer<B> {

  @Override
  public long getTag() {
    return 1337;
  }

  @Override
  public void serializeContents(B metrics, DataOutput output) throws IOException {}

  @Override
  public boolean deserializeContents(B metrics, DataInput input) throws IOException {
    return false;
  }
}
