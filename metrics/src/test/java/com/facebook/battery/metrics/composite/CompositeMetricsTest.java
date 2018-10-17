/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.composite;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.core.SystemMetricsTest;
import com.facebook.battery.metrics.cpu.CpuMetrics;
import com.facebook.battery.metrics.time.TimeMetrics;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CompositeMetricsTest extends SystemMetricsTest<CompositeMetrics> {

  public @Rule ExpectedException mThrown = ExpectedException.none();

  @Override
  public void testNullOutput() throws Exception {
    mThrown.expect(IllegalArgumentException.class);
    super.testNullOutput();
  }

  @Override
  protected Class<CompositeMetrics> getClazz() {
    return CompositeMetrics.class;
  }

  @Override
  protected CompositeMetrics createInstance() throws Exception {
    return new CompositeMetrics().putMetric(TimeMetrics.class, new TimeMetrics());
  }

  @Override
  public void testSum() throws Exception {
    CompositeMetrics metricsA = createInitializedInstance();
    CompositeMetrics metricsB = createInitializedInstance();

    CompositeMetrics sum = createInstance();
    metricsA.sum(metricsB, sum);

    assertThat(sum.getMetrics().size()).isEqualTo(1);
    assertThat(sum.getMetric(TimeMetrics.class).realtimeMs).isEqualTo(200);
    assertThat(sum.getMetric(TimeMetrics.class).uptimeMs).isEqualTo(400);
  }

  @Override
  public void testDiff() throws Exception {
    CompositeMetrics metricsA = createInitializedInstance();
    CompositeMetrics metricsB = createInitializedInstance();

    CompositeMetrics diff = createInstance();
    metricsA.diff(metricsB, diff);

    assertThat(diff.getMetrics().size()).isEqualTo(1);
    assertThat(diff.getMetric(TimeMetrics.class).realtimeMs).isEqualTo(0);
    assertThat(diff.getMetric(TimeMetrics.class).uptimeMs).isEqualTo(0);
  }

  @Override
  protected CompositeMetrics createInitializedInstance() throws Exception {
    CompositeMetrics metrics = new CompositeMetrics();
    TimeMetrics timeMetrics = new TimeMetrics();
    metrics.putMetric(TimeMetrics.class, timeMetrics);

    timeMetrics.realtimeMs = 100;
    timeMetrics.uptimeMs = 200;
    metrics.setIsValid(TimeMetrics.class, true);

    return metrics;
  }

  @Test
  public void testInconsistentSet() throws Exception {
    CompositeMetrics metricsA =
        new CompositeMetrics().putMetric(TimeMetrics.class, new TimeMetrics());
    CompositeMetrics metricsB =
        new CompositeMetrics().putMetric(CpuMetrics.class, new CpuMetrics());
    metricsA.set(metricsB);

    assertThat(metricsA.isValid(TimeMetrics.class)).isFalse();
  }

  @Test
  public void testInconsistentSum() throws Exception {
    CompositeMetrics sum = new CompositeMetrics().putMetric(CpuMetrics.class, new CpuMetrics());
    CompositeMetrics metricsA =
        new CompositeMetrics().putMetric(TimeMetrics.class, new TimeMetrics());
    CompositeMetrics metricsB =
        new CompositeMetrics().putMetric(CpuMetrics.class, new CpuMetrics());

    metricsA.sum(metricsB, sum);

    assertThat(sum.isValid(TimeMetrics.class)).isFalse();
    assertThat(sum.isValid(CpuMetrics.class)).isFalse();
  }

  @Test
  public void testInconsistentDiff() throws Exception {
    CompositeMetrics diff = new CompositeMetrics().putMetric(CpuMetrics.class, new CpuMetrics());
    CompositeMetrics metricsA =
        new CompositeMetrics().putMetric(TimeMetrics.class, new TimeMetrics());
    CompositeMetrics metricsB =
        new CompositeMetrics().putMetric(CpuMetrics.class, new CpuMetrics());

    metricsA.diff(metricsB, diff);

    assertThat(diff.isValid(TimeMetrics.class)).isFalse();
    assertThat(diff.isValid(CpuMetrics.class)).isFalse();
  }

  @Test
  public void testValidInvalidSum() throws Exception {
    CompositeMetrics metricsA = createInitializedInstance();
    CompositeMetrics metricsB = createInitializedInstance();
    metricsA.setIsValid(TimeMetrics.class, false);

    CompositeMetrics sum = createInstance();
    metricsA.sum(metricsB, sum);

    assertThat(sum.getMetrics().size()).isEqualTo(1);
    assertThat(sum.getMetric(TimeMetrics.class).realtimeMs).isEqualTo(100);
    assertThat(sum.getMetric(TimeMetrics.class).uptimeMs).isEqualTo(200);
    assertThat(sum.isValid(TimeMetrics.class)).isTrue();
  }
}
