/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CpuFrequencyMetricsCollectorTest
    extends SystemMetricsCollectorTest<CpuFrequencyMetrics, CpuFrequencyMetricsCollector> {

  TemporaryFolder mFolder = new TemporaryFolder();

  @BeforeClass
  public static void overrideCores() throws Exception {
    CpuFrequencyMetricsCollector.overrideCores();
  }

  @Before
  public void setUp() throws Exception {
    mFolder.create();
  }

  @Test
  public void testValidCores() throws Exception {
    TestableCpuFrequencyMetricsCollector collector =
        new TestableCpuFrequencyMetricsCollector(
            new String[] {
              createFile("123 45600\n78910 11121300"),
              createFile("987 12300\n888 555500\n666 55500"),
              createFile("1 100"),
              createFile("2 200"),
            });

    CpuFrequencyMetrics metrics = collector.createMetrics();
    assertThat(collector.getSnapshot(metrics)).isTrue();

    assertThat(metrics.timeInStateS.length).isEqualTo(4);
    assertThat(metrics.timeInStateS[0].size()).isEqualTo(2);
    assertThat(metrics.timeInStateS[1].size()).isEqualTo(3);
    assertThat(metrics.timeInStateS[2].size()).isEqualTo(1);
    assertThat(metrics.timeInStateS[3].size()).isEqualTo(1);

    assertThat(metrics.timeInStateS[0].get(123)).isEqualTo(456);
    assertThat(metrics.timeInStateS[0].get(78910)).isEqualTo(111213);

    assertThat(metrics.timeInStateS[1].get(987)).isEqualTo(123);
    assertThat(metrics.timeInStateS[1].get(888)).isEqualTo(5555);
    assertThat(metrics.timeInStateS[1].get(666)).isEqualTo(555);

    assertThat(metrics.timeInStateS[2].get(1)).isEqualTo(1);
    assertThat(metrics.timeInStateS[3].get(2)).isEqualTo(2);
  }

  @Test
  public void testPartialMissingCores() throws Exception {
    TestableCpuFrequencyMetricsCollector collector =
        new TestableCpuFrequencyMetricsCollector(
            new String[] {
              createFile("1 200"), createFile("3 400"), "invalid path", "likewise",
            });

    CpuFrequencyMetrics metrics = collector.createMetrics();
    assertThat(collector.getSnapshot(metrics)).isTrue();

    assertThat(metrics.timeInStateS.length).isEqualTo(4);
    assertThat(metrics.timeInStateS[0].size()).isEqualTo(1);
    assertThat(metrics.timeInStateS[1].size()).isEqualTo(1);
    assertThat(metrics.timeInStateS[2].size()).isEqualTo(0);
    assertThat(metrics.timeInStateS[3].size()).isEqualTo(0);

    assertThat(metrics.timeInStateS[0].get(1)).isEqualTo(2);
    assertThat(metrics.timeInStateS[1].get(3)).isEqualTo(4);
  }

  @Test
  public void testInvalidContents() throws Exception {
    TestableCpuFrequencyMetricsCollector collector =
        new TestableCpuFrequencyMetricsCollector(
            new String[] {
              createFile("test"), createFile("1 100"), createFile("2 200"), createFile("3 300"),
            });

    CpuFrequencyMetrics metrics = collector.createMetrics();
    assertThat(collector.getSnapshot(metrics)).isTrue();

    assertThat(metrics.timeInStateS.length).isEqualTo(4);
    assertThat(metrics.timeInStateS[0].size()).isEqualTo(0);
  }

  @Test
  public void testAllMissingCores() throws Exception {
    TestableCpuFrequencyMetricsCollector collector =
        new TestableCpuFrequencyMetricsCollector(
            new String[] {"everything", "is", "horribly", "broken"});

    CpuFrequencyMetrics metrics = collector.createMetrics();
    assertThat(collector.getSnapshot(metrics)).isFalse();
  }

  private String createFile(String contents) throws IOException {
    File file = mFolder.newFile();
    FileOutputStream os = new FileOutputStream(file, false);
    os.write(contents.getBytes());
    return file.getAbsolutePath();
  }

  @Override
  protected Class<CpuFrequencyMetricsCollector> getClazz() {
    return CpuFrequencyMetricsCollector.class;
  }
}

class TestableCpuFrequencyMetricsCollector extends CpuFrequencyMetricsCollector {

  private final String[] mPaths;

  public TestableCpuFrequencyMetricsCollector(String[] paths) {
    mPaths = paths;
  }

  @Override
  protected synchronized String getPath(int core) {
    return mPaths[core];
  }
}
