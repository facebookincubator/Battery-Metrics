/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.cpu;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.core.ReporterEvent;
import com.facebook.battery.metrics.cpu.CpuFrequencyMetrics;
import com.facebook.battery.metrics.cpu.CpuFrequencyMetricsCollector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CpuFrequencyMetricsReporterTest {

  private CpuFrequencyMetricsReporter mReporter;
  private CpuFrequencyMetrics mMetrics;
  private ReporterEvent mEvent;

  @BeforeClass
  public static void overrideCores() {
    CpuFrequencyMetricsCollector.overrideCores();
  }

  @Before
  public void setUp() {
    mReporter = new CpuFrequencyMetricsReporter();
    mMetrics = new CpuFrequencyMetrics();
    mEvent = new ReporterEvent();
  }

  @Test
  public void testZeroLogging() {
    mReporter.reportTo(mMetrics, mEvent);
    assertThat(mEvent.eventMap.isEmpty()).isTrue();
  }
}
