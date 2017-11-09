/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.composite;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.support.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CompositeMetricsCollectorTest {

  private ACollector mACollector;
  private BCollector mBCollector;
  private CompositeMetricsCollector mCollector;
  private CompositeMetrics mMetrics;

  @Before
  public void setUp() throws Exception {
    mACollector = new ACollector();
    mBCollector = new BCollector();

    mCollector =
        new CompositeMetricsCollector.Builder()
            .addMetricsCollector(A.class, mACollector)
            .addMetricsCollector(B.class, mBCollector)
            .build();
    mMetrics = new CompositeMetrics().putMetric(A.class, new A()).putMetric(B.class, new B());
  }

  @Test
  public void allSnapshotsSucceed() throws Exception {
    mACollector.succeeds = true;
    mACollector.currentValue = 100;
    mBCollector.succeeds = true;
    mBCollector.currentValue = 120;

    assertThat(mCollector.getSnapshot(mMetrics)).isTrue();
    assertThat(mMetrics.getMetrics().size()).isEqualTo(2);
    assertThat(mMetrics.getMetric(A.class).value).isEqualTo(100);
    assertThat(mMetrics.isValid(A.class)).isEqualTo(true);
    assertThat(mMetrics.getMetric(B.class).value).isEqualTo(120);
    assertThat(mMetrics.isValid(B.class)).isEqualTo(true);
  }

  @Test
  public void partialFailures() throws Exception {
    mACollector.succeeds = true;
    mACollector.currentValue = 100;
    mBCollector.succeeds = false;

    assertThat(mCollector.getSnapshot(mMetrics)).isTrue();
    assertThat(mMetrics.getMetrics().size()).isEqualTo(2);
    assertThat(mMetrics.getMetric(A.class).value).isEqualTo(100);
    assertThat(mMetrics.isValid(A.class)).isEqualTo(true);
    assertThat(mMetrics.isValid(B.class)).isEqualTo(false);
  }

  @Test
  public void allSnapshotsFail() throws Exception {
    mACollector.succeeds = false;
    mBCollector.succeeds = false;

    assertThat(mCollector.getSnapshot(mMetrics)).isFalse();
    assertThat(mMetrics.getMetrics().size()).isEqualTo(2);
    assertThat(mMetrics.isValid(A.class)).isEqualTo(false);
    assertThat(mMetrics.isValid(B.class)).isEqualTo(false);
  }

  @Test
  public void subsetSnapshots() throws Exception {
    mACollector.currentValue = 100;
    mACollector.succeeds = true;
    CompositeMetrics m = new CompositeMetrics().putMetric(A.class, new A());
    mCollector.getSnapshot(m);

    assertThat(mCollector.getSnapshot(m)).isTrue();
    assertThat(m.getMetrics().size()).isEqualTo(1);
    assertThat(m.getMetric(A.class).value).isEqualTo(100);
    assertThat(m.isValid(A.class)).isEqualTo(true);

    assertThat(m.getMetric(B.class)).isNull();
  }

  @Test
  public void supersetSnapshots() throws Exception {
    mACollector.currentValue = 100;
    mACollector.succeeds = true;
    CompositeMetrics m =
        new CompositeMetrics().putMetric(A.class, new A()).putMetric(C.class, new C());
    mCollector.getSnapshot(m);

    assertThat(mCollector.getSnapshot(m)).isTrue();
    assertThat(m.getMetrics().size()).isEqualTo(2);
    assertThat(m.getMetric(A.class).value).isEqualTo(100);
    assertThat(m.isValid(A.class)).isEqualTo(true);
    assertThat(m.getMetric(B.class)).isNull();
    assertThat(m.isValid(C.class)).isFalse();
  }
}

class A extends SystemMetrics<A> {

  int value;

  @Override
  public A sum(@Nullable A b, @Nullable A output) {
    output.value = b.value + value;
    return output;
  }

  @Override
  public A diff(@Nullable A b, @Nullable A output) {
    output.value = value - b.value;
    return output;
  }

  @Override
  public A set(A b) {
    value = b.value;
    return this;
  }
}

class B extends SystemMetrics<B> {

  int value;

  @Override
  public B sum(@Nullable B b, @Nullable B output) {
    output.value = b.value + value;
    return output;
  }

  @Override
  public B diff(@Nullable B b, @Nullable B output) {
    output.value = value - b.value;
    return output;
  }

  @Override
  public B set(B b) {
    value = b.value;
    return this;
  }
}

class ACollector extends SystemMetricsCollector<A> {

  int currentValue = 0;
  boolean succeeds = true;

  @Override
  public boolean getSnapshot(A snapshot) {
    snapshot.value = currentValue;
    return succeeds;
  }

  @Override
  public A createMetrics() {
    return new A();
  }
}

class BCollector extends SystemMetricsCollector<B> {

  int currentValue = 0;
  boolean succeeds = true;

  @Override
  public boolean getSnapshot(B snapshot) {
    snapshot.value = currentValue;
    return succeeds;
  }

  @Override
  public B createMetrics() {
    return new B();
  }
}

class C extends SystemMetrics<C> {

  @Override
  public C sum(@Nullable C b, @Nullable C output) {
    return null;
  }

  @Override
  public C diff(@Nullable C b, @Nullable C output) {
    return null;
  }

  @Override
  public C set(C b) {
    return null;
  }
}
