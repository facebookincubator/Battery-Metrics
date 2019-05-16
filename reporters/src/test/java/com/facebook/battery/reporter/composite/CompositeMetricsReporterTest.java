/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.reporter.composite;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.core.ReporterEvent;
import com.facebook.battery.metrics.time.TimeMetrics;
import com.facebook.battery.reporter.time.TimeMetricsReporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CompositeMetricsReporterTest {

  private CompositeMetricsReporter mReporter;
  private ReporterEvent mEvent;

  @Before
  public void setUp() {
    mReporter = new CompositeMetricsReporter();
    mReporter.addMetricsReporter(TimeMetrics.class, new TimeMetricsReporter());
    mEvent = new ReporterEvent();
  }

  @Test
  public void testInvalidMetricsSkipped() {
    TimeMetrics timeMetrics = new TimeMetrics();
    timeMetrics.realtimeMs = 100;
    timeMetrics.uptimeMs = 10;
    CompositeMetrics metrics = new CompositeMetrics().putMetric(TimeMetrics.class, timeMetrics);
    metrics.setIsValid(TimeMetrics.class, false);

    mReporter.reportTo(metrics, mEvent);
    assertThat(mEvent.eventMap.size()).isEqualTo(0);
  }

  @Test
  public void testValidMetricsLogged() {
    TimeMetrics timeMetrics = new TimeMetrics();
    timeMetrics.realtimeMs = 100;
    timeMetrics.uptimeMs = 10;
    CompositeMetrics metrics = new CompositeMetrics().putMetric(TimeMetrics.class, timeMetrics);
    metrics.setIsValid(TimeMetrics.class, true);

    mReporter.reportTo(metrics, mEvent);
    assertThat(mEvent.eventMap.size()).isEqualTo(2);
  }
}
