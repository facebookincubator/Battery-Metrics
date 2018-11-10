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
    verify(logger, never()).wtf(anyString(), anyString(), any(Throwable.class));

    StringBuilder secondEntry = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      secondEntry.append(i * 1000).append(' ');
    }
    overwriteFile(new File(path), secondEntry.toString());
    assertThat(collector.getSnapshot(snapshot)).isFalse();
    verify(logger, times(1)).wtf(anyString(), anyString(), any(Throwable.class));
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
