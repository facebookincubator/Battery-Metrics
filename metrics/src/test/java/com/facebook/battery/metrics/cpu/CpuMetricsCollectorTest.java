/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CpuMetricsCollectorTest
    extends SystemMetricsCollectorTest<CpuMetrics, CpuMetricsCollector> {

  TemporaryFolder mFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    mFolder.create();
  }

  @Test
  public void testBrokenFile() throws Exception {
    TestableCpuMetricsCollector collector =
        new TestableCpuMetricsCollector().setPath(createFile("I am a weird android manufacturer"));

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isFalse();
  }

  @Test
  public void testNegativeFields() throws Exception {
    StringBuilder testStringBuilder = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      testStringBuilder.append(-i * 100).append(' ');
    }
    TestableCpuMetricsCollector collector =
        new TestableCpuMetricsCollector().setPath(createFile(testStringBuilder.toString()));

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isFalse();
  }

  @Test
  public void testErrorOnDecreasing() throws Exception {
    SystemMetricsLogger.Delegate logger = mock(SystemMetricsLogger.Delegate.class);
    SystemMetricsLogger.setDelegate(logger);

    StringBuilder initialEntry = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      initialEntry.append(i * 2000).append(' ');
    }
    String path = createFile(initialEntry.toString());
    TestableCpuMetricsCollector collector = new TestableCpuMetricsCollector().setPath(path);
    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();
    verify(logger, never()).wtf(anyString(), anyString(), (Throwable) any());

    StringBuilder secondEntry = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      secondEntry.append(i * 1000).append(' ');
    }
    overwriteFile(new File(path), secondEntry.toString());
    assertThat(collector.getSnapshot(snapshot)).isFalse();
    verify(logger, times(1)).wtf(anyString(), anyString(), (Throwable) any());
  }

  @Test
  public void testRealProcfile() throws Exception {
    String stat =
        "21031 (facebook.katana) S 354 354 0 0 -1 1077952832 227718 1446 318 0 9852 889 6 11 20 0"
            + " 133 0 502496 2050461696 70553 4294967295 1 1 0 0 0 0 4608 0 1166120188 4294967295 0"
            + " 0 17 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
    TestableCpuMetricsCollector collector =
        new TestableCpuMetricsCollector().setPath(createFile(stat));

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();

    assertThat(snapshot.userTimeS).isEqualTo(9852.0 / 100);
    assertThat(snapshot.systemTimeS).isEqualTo(889.0 / 100);
    assertThat(snapshot.childUserTimeS).isEqualTo(6.0 / 100);
    assertThat(snapshot.childSystemTimeS).isEqualTo(11.0 / 100);
  }

  @Test
  public void testRealProcfileWithBlankedComm() throws Exception {
    String stat =
        "21031 (facebook.blank katana) S 354 354 0 0 -1 1077952832 227718 1446 318 0 9852 889 6 11 20 0 133 0 502496 2050461696 70553 4294967295 1 1 0 0 0 0 4608 0 1166120188 4294967295 0 0 17 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
    TestableCpuMetricsCollector collector =
        new TestableCpuMetricsCollector().setPath(createFile(stat));

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();

    assertThat(snapshot.userTimeS).isEqualTo(9852.0 / 100);
    assertThat(snapshot.systemTimeS).isEqualTo(889.0 / 100);
    assertThat(snapshot.childUserTimeS).isEqualTo(6.0 / 100);
    assertThat(snapshot.childSystemTimeS).isEqualTo(11.0 / 100);
  }

  @Test
  public void testSaneProcFile() throws Exception {
    StringBuilder testStringBuilder = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      testStringBuilder.append(i * 100).append(' ');
    }
    TestableCpuMetricsCollector collector =
        new TestableCpuMetricsCollector().setPath(createFile(testStringBuilder.toString()));

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();

    assertThat(snapshot.userTimeS).isEqualTo(13);
    assertThat(snapshot.systemTimeS).isEqualTo(14);
    assertThat(snapshot.childUserTimeS).isEqualTo(15);
    assertThat(snapshot.childSystemTimeS).isEqualTo(16);
  }

  @Test
  public void testUnreadableProcFile() throws Exception {
    TestableCpuMetricsCollector collector = new TestableCpuMetricsCollector().setPath("");

    CpuMetrics snapshot = new CpuMetrics();
    assertThat(collector.getSnapshot(snapshot)).isFalse();
  }

  private String createFile(String contents) throws IOException {
    File file = mFolder.newFile();
    return overwriteFile(file, contents);
  }

  private static String overwriteFile(File file, String contents) throws IOException {
    FileOutputStream os = new FileOutputStream(file, false);
    os.write(contents.getBytes());
    os.close();
    return file.getCanonicalPath();
  }

  @Override
  protected Class<CpuMetricsCollector> getClazz() {
    return CpuMetricsCollector.class;
  }
}

class TestableCpuMetricsCollector extends CpuMetricsCollector {

  private String mPath;

  public synchronized TestableCpuMetricsCollector setPath(String path) {
    mPath = path;
    return this;
  }

  @Override
  protected synchronized String getPath() {
    return mPath;
  }
}
