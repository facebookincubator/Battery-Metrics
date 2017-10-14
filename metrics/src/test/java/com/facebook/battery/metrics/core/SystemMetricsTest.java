/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
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
    T instanceA = MetricsUtil.createInitializedInstance(getClazz());
    T instanceB = MetricsUtil.createInitializedInstance(getClazz());

    assertThat(instanceA).isEqualTo(instanceB);
  }

  @Test
  public void testDefaultValues() throws Exception {
    T t = getClazz().newInstance();
    for (Field field : getClazz().getFields()) {
      if (MetricsUtil.isNumericField(field)) {
        MetricsUtil.testValue(t, field, 0);
      }
    }
  }

  @Test
  public void testSet() throws Exception {
    T a = MetricsUtil.createInitializedInstance(getClazz());
    T empty = getClazz().newInstance();
    empty.set(a);

    assertThat(empty).isEqualTo(a);
  }

  @Test
  public void testSum() throws Exception {
    T a = MetricsUtil.createInitializedInstance(getClazz());
    int increment = 1;
    for (Field field : getClazz().getFields()) {
      if (MetricsUtil.isNumericField(field)) {
        field.set(a, 2 * increment);
        increment += 1;
      }
    }

    T b = MetricsUtil.createInitializedInstance(getClazz());
    T sum = getClazz().newInstance();

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
    T a = MetricsUtil.createInitializedInstance(getClazz());
    T b = MetricsUtil.createInitializedInstance(getClazz());
    T diff = MetricsUtil.createInitializedInstance(getClazz());

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
    T instanceA = MetricsUtil.createInitializedInstance(getClazz());
    T instanceB = MetricsUtil.createInitializedInstance(getClazz());

    T diff = instanceA.diff(instanceB, null);
    assertThat(diff).isNotNull();
  }

  @Test
  public void testNullSubtrahend() throws Exception {
    T instanceA = MetricsUtil.createInitializedInstance(getClazz());
    T diff = MetricsUtil.createInitializedInstance(getClazz());
    instanceA.diff(null, diff);
    assertThat(instanceA).isEqualTo(diff);
  }

  protected abstract Class<T> getClazz();
}
