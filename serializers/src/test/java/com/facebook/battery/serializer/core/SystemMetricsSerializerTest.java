/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.core;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.core.MetricsUtil;
import com.facebook.battery.metrics.core.SystemMetrics;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.junit.Test;

public abstract class SystemMetricsSerializerTest<T extends SystemMetrics<T>> {

  @Test
  public void testSerialization() throws Exception {
    T instance = createInitializedInstance();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getSerializer().serialize(instance, new DataOutputStream(baos));

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    T output = createInstance();

    assertThat(getSerializer().deserialize(output, new DataInputStream(bais))).isTrue();
    assertThat(output).isEqualTo(instance);
  }

  @Test
  public void testSerializeContents() throws Exception {
    T instance = createInitializedInstance();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getSerializer().serializeContents(instance, new DataOutputStream(baos));

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    T output = createInstance();

    assertThat(getSerializer().deserializeContents(output, new DataInputStream(bais))).isTrue();
    assertThat(output).isEqualTo(instance);
  }

  @Test
  public void testRandomContents() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream daos = new DataOutputStream(baos);
    daos.writeChars("Random test string that doesn't make any sense");

    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
    T output = createInstance();
    assertThat(getSerializer().deserialize(output, dis)).isFalse();
  }

  protected abstract Class<T> getClazz();

  protected abstract SystemMetricsSerializer<T> getSerializer();

  protected T createInitializedInstance() throws Exception {
    return MetricsUtil.createInitializedInstance(getClazz());
  }

  protected T createInstance() throws Exception {
    return getClazz().newInstance();
  }
}
