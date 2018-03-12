/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.cpu;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CpuMetricsCollectorTest
    extends SystemMetricsCollectorTest<CpuMetrics, CpuMetricsCollector> {

  @Test
  public void testBrokenFile() {
    TestableCpuMetricsCollector collector = new TestableCpuMetricsCollector()
        .setProcFileContents("I am a weird android manufacturer");

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isFalse();
  }

  @Test
  public void testNegativeFields() {
    StringBuilder testStringBuilder = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      testStringBuilder.append(-i * 100).append(' ');
    }
    TestableCpuMetricsCollector collector = new TestableCpuMetricsCollector()
        .setProcFileContents(testStringBuilder.toString());

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isFalse();
  }

  @Test
  public void testErrorOnDecreasing() {
    SystemMetricsLogger.Delegate logger = mock(SystemMetricsLogger.Delegate.class);
    SystemMetricsLogger.setDelegate(logger);

    StringBuilder initialEntry = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      initialEntry.append(i * 2).append(' ');
    }
    TestableCpuMetricsCollector collector =
        new TestableCpuMetricsCollector().setProcFileContents(initialEntry.toString());
    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();
    verify(logger, never()).wtf(anyString(), anyString(), any(Throwable.class));

    StringBuilder secondEntry = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      secondEntry.append(i).append(' ');
    }
    collector.setProcFileContents(secondEntry.toString());
    assertThat(collector.getSnapshot(snapshot)).isFalse();
    verify(logger, times(1)).wtf(anyString(), anyString(), any(Throwable.class));
  }

  @Test
  public void testSaneProcFile() {
    StringBuilder testStringBuilder = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      testStringBuilder.append(i * 100).append(' ');
    }
    TestableCpuMetricsCollector collector = new TestableCpuMetricsCollector()
        .setProcFileContents(testStringBuilder.toString());

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();

    assertThat(snapshot.userTimeS).isEqualTo(13);
    assertThat(snapshot.systemTimeS).isEqualTo(14);
    assertThat(snapshot.childUserTimeS).isEqualTo(15);
    assertThat(snapshot.childSystemTimeS).isEqualTo(16);
  }

  @Test
  public void testUnreadableProcFile() {
    TestableCpuMetricsCollector collector = new TestableCpuMetricsCollector()
        .setProcFileContents(null);

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isFalse();
  }

  @Override
  protected Class<CpuMetricsCollector> getClazz() {
    return CpuMetricsCollector.class;
  }
}

class TestableCpuMetricsCollector extends CpuMetricsCollector {

  private String mContents;

  public TestableCpuMetricsCollector setProcFileContents(String contents) {
    mContents = contents;
    return this;
  }

  @Override
  protected String readProcFile() {
    return mContents;
  }
}
