/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.core;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.lang.reflect.Field;
import org.junit.Test;

/**
 * Very opinionated base set of tests for SystemMetrics to reduce boilerplate at the cost of some
 * extra reflection.
 */
public abstract class SystemMetricsTest<T extends SystemMetrics<T>> {

  @Test
  public void testEquals() throws Exception {
    T instanceA = createInitializedInstance();
    T instanceB = createInitializedInstance();

    assertThat(instanceA).isEqualTo(instanceB);
  }

  @Test
  public void testDefaultValues() throws Exception {
    T t = createInstance();
    for (Field field : getClazz().getFields()) {
      if (MetricsUtil.isNumericField(field)) {
        MetricsUtil.testValue(t, field, 0);
      }
    }
  }

  @Test
  public void testSet() throws Exception {
    T a = createInitializedInstance();
    T empty = createInstance();
    empty.set(a);

    assertThat(empty).isEqualTo(a);
  }

  @Test
  public void testSum() throws Exception {
    T a = createInitializedInstance();
    int increment = 1;
    for (Field field : getClazz().getFields()) {
      if (MetricsUtil.isNumericField(field)) {
        field.set(a, 2 * increment);
        increment += 1;
      }
    }

    T b = createInitializedInstance();
    T sum = createInstance();

    a.sum(b, sum);
    increment = 1;
    for (Field field : getClazz().getFields()) {
      if (MetricsUtil.isNumericField(field)) {
        MetricsUtil.testValue(sum, field, 3 * increment);
        increment += 1;
      }
    }
  }

  @Test
  public void testDiff() throws Exception {
    T a = createInitializedInstance();
    T b = createInitializedInstance();
    T diff = createInitializedInstance();

    int index = 1;
    for (Field field : getClazz().getFields()) {
      if (MetricsUtil.isNumericField(field)) {
        field.set(a, 2 * index);
        index += 1;
      }
    }

    a.diff(b, diff);

    int increment = 1;
    for (Field field : getClazz().getFields()) {
      if (MetricsUtil.isNumericField(field)) {
        MetricsUtil.testValue(diff, field, increment);
        increment += 1;
      }
    }
  }

  @Test
  public void testNullOutput() throws Exception {
    T instanceA = createInitializedInstance();
    T instanceB = createInitializedInstance();

    T diff = instanceA.diff(instanceB, null);
    assertThat(diff).isNotNull();
  }

  @Test
  public void testNullSubtrahend() throws Exception {
    T instanceA = createInitializedInstance();
    T diff = createInitializedInstance();
    instanceA.diff(null, diff);
    assertThat(instanceA).isEqualTo(diff);
  }

  protected abstract Class<T> getClazz();

  protected T createInstance() throws Exception {
    return getClazz().newInstance();
  }

  protected T createInitializedInstance() throws Exception {
    return MetricsUtil.createInitializedInstance(getClazz());
  }
}
