/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.composite;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.core.ReporterEvent;
import com.facebook.battery.metrics.time.TimeMetrics;
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
    mEvent = new ReporterEvent();
  }

  @Test
  public void testInvalidMetricsSkipped() {
    CompositeMetrics metrics =
        new CompositeMetrics().putMetric(TimeMetrics.class, new TimeMetrics());
    metrics.setIsValid(TimeMetrics.class, false);

    mReporter.reportTo(metrics, mEvent);
    assertThat(mEvent.eventMap.size()).isEqualTo(0);
  }

  @Test
  public void testValidMetricsLogged() {
    CompositeMetrics metrics =
        new CompositeMetrics().putMetric(TimeMetrics.class, new TimeMetrics());
    metrics.setIsValid(TimeMetrics.class, false);

    mReporter.reportTo(metrics, mEvent);
    assertThat(mEvent.eventMap.size()).isEqualTo(0);
  }
}
