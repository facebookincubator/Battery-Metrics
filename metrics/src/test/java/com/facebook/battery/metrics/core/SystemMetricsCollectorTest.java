package com.facebook.battery.metrics.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class SystemMetricsCollectorTest<
    S extends SystemMetrics, T extends SystemMetricsCollector<S>> {

  @Rule public final ExpectedException mExpectedException = ExpectedException.none();

  @Test
  public void testNullSnapshot() throws Exception {
    mExpectedException.expect(IllegalArgumentException.class);
    T instance = getClazz().newInstance();
    instance.getSnapshot(null);
  }

  protected abstract Class<T> getClazz();
}
