/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.cpu;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.reporter.core.ReporterEvent;
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

  @Test
  public void testSingleCore() {
    mMetrics.timeInStateS[0].put(100, 100);
    mReporter.reportTo(mMetrics, mEvent);
    assertThat(mEvent.eventMap.isEmpty()).isFalse();
    assertThat(mEvent.eventMap.get(CpuFrequencyMetricsReporter.CPU_TIME_IN_STATE_S))
        .isEqualTo("{\"1\":{\"100\":100}}");
  }

  @Test
  public void testMultipleCores() {
    mMetrics.timeInStateS[0].put(100, 100);
    mMetrics.timeInStateS[2].put(200, 200);
    mReporter.reportTo(mMetrics, mEvent);
    assertThat(mEvent.eventMap.isEmpty()).isFalse();
    assertThat(mEvent.eventMap.get(CpuFrequencyMetricsReporter.CPU_TIME_IN_STATE_S))
        .isEqualTo("{\"1\":{\"100\":100},\"4\":{\"200\":200}}");
  }

  @Test
  public void testCoreCombination() {
    mMetrics.timeInStateS[0].put(100, 100);
    mMetrics.timeInStateS[2].put(100, 100);
    mMetrics.timeInStateS[1].put(200, 200);
    mMetrics.timeInStateS[3].put(200, 200);

    mReporter.reportTo(mMetrics, mEvent);
    assertThat(mEvent.eventMap.isEmpty()).isFalse();
    assertThat(mEvent.eventMap.get(CpuFrequencyMetricsReporter.CPU_TIME_IN_STATE_S))
        .isEqualTo("{\"a\":{\"200\":200},\"5\":{\"100\":100}}");
  }
}
