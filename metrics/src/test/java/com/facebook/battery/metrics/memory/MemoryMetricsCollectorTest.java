/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.memory;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.facebook.battery.metrics.core.ShadowDebug;
import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(shadows = {ShadowDebug.class})
public class MemoryMetricsCollectorTest
    extends SystemMetricsCollectorTest<MemoryMetrics, MemoryMetricsCollector> {

  TemporaryFolder mFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    mFolder.create();
  }

  @Test
  public void testBrokenFile() throws Exception {
    MemoryMetricsCollectorWithProcFile collector =
        new MemoryMetricsCollectorWithProcFile()
            .setPath(createFile("I am a weird android manufacturer"));

    MemoryMetrics snapshot = new MemoryMetrics();
    collector.getSnapshot(snapshot);
    assertThat(collector.getSnapshot(snapshot)).isTrue();

    assertThat(snapshot.vmSizeKb).isEqualTo(-1);
    assertThat(snapshot.vmRssKb).isEqualTo(-1);
  }

  @Test
  public void testVirtualMemory() throws Exception {
    String statm = "4 2 0 4 0 0 0";
    MemoryMetricsCollectorWithProcFile collector =
        new MemoryMetricsCollectorWithProcFile().setPath(createFile(statm));

    MemoryMetrics snapshot = new MemoryMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();

    assertThat(snapshot.vmSizeKb).isEqualTo(16);
    assertThat(snapshot.vmRssKb).isEqualTo(8);
  }

  @Test
  public void testDefaultDisabled() {
    ShadowDebug.setNativeHeapSize(4 * 1024);
    ShadowDebug.setNativeHeapAllocatedSize(3 * 1024);

    MemoryMetrics snapshot = new MemoryMetrics();
    MemoryMetricsCollector collector = new MemoryMetricsCollector();
    collector.getSnapshot(snapshot);

    assertThat(collector.getSnapshot(snapshot)).isFalse();
    assertThat(snapshot.nativeHeapSizeKb).isEqualTo(0);
    assertThat(snapshot.nativeHeapAllocatedKb).isEqualTo(0);
  }

  @Test
  public void testNativeHeap() {
    ShadowDebug.setNativeHeapSize(4 * 1024);
    ShadowDebug.setNativeHeapAllocatedSize(3 * 1024);

    MemoryMetrics snapshot = new MemoryMetrics();
    MemoryMetricsCollector collector = new MemoryMetricsCollector();
    collector.enable();
    collector.getSnapshot(snapshot);

    assertThat(snapshot.nativeHeapSizeKb).isEqualTo(4);
    assertThat(snapshot.nativeHeapAllocatedKb).isEqualTo(3);
  }

  @Test
  public void testSnapshotSequenceNumber() {
    MemoryMetricsCollector collector = new MemoryMetricsCollector();
    collector.enable();

    MemoryMetrics first = new MemoryMetrics();
    MemoryMetrics second = new MemoryMetrics();

    collector.getSnapshot(first);
    collector.getSnapshot(second);

    assertThat(first.sequenceNumber).isEqualTo(1);
    assertThat(second.sequenceNumber).isEqualTo(2);
  }

  @Test
  public void testNativeHeapSumFirstWithSecond() {
    MemoryMetrics first = new MemoryMetrics();
    MemoryMetrics second = new MemoryMetrics();
    MemoryMetrics output = new MemoryMetrics();

    MemoryMetricsCollector collector = new MemoryMetricsCollector();
    collector.enable();

    ShadowDebug.setNativeHeapSize(4 * 1024);
    ShadowDebug.setNativeHeapAllocatedSize(3 * 1024);
    collector.getSnapshot(first);

    ShadowDebug.setNativeHeapSize(8 * 1024);
    ShadowDebug.setNativeHeapAllocatedSize(6 * 1024);
    collector.getSnapshot(second);

    first.sum(second, output);
    assertThat(output.nativeHeapSizeKb).isEqualTo(8);
    assertThat(output.nativeHeapAllocatedKb).isEqualTo(6);
  }

  @Test
  public void testNativeHeapSumSecondWithFirst() {
    MemoryMetrics first = new MemoryMetrics();
    MemoryMetrics second = new MemoryMetrics();
    MemoryMetrics output = new MemoryMetrics();

    MemoryMetricsCollector collector = new MemoryMetricsCollector();
    collector.enable();

    ShadowDebug.setNativeHeapSize(4 * 1024);
    ShadowDebug.setNativeHeapAllocatedSize(3 * 1024);
    collector.getSnapshot(first);

    ShadowDebug.setNativeHeapSize(8 * 1024);
    ShadowDebug.setNativeHeapAllocatedSize(6 * 1024);
    collector.getSnapshot(second);

    second.sum(first, output);
    assertThat(output.nativeHeapSizeKb).isEqualTo(8);
    assertThat(output.nativeHeapAllocatedKb).isEqualTo(6);
  }

  @Test
  public void testJavaHeap() {
    Runtime runtime = mock(Runtime.class);
    MemoryMetricsCollectorWithRuntime collector =
        new MemoryMetricsCollectorWithRuntime().setRuntime(runtime);
    MemoryMetrics snapshot = new MemoryMetrics();

    when(runtime.maxMemory()).thenReturn(8 * 1024l);
    when(runtime.totalMemory()).thenReturn(6 * 1024l);

    collector.getSnapshot(snapshot);
    assertThat(snapshot.javaHeapMaxSizeKb).isEqualTo(8);
    assertThat(snapshot.nativeHeapAllocatedKb).isEqualTo(6);
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
  protected Class<MemoryMetricsCollector> getClazz() {
    return MemoryMetricsCollector.class;
  }
}

class MemoryMetricsCollectorWithProcFile extends MemoryMetricsCollector {

  private String mPath;

  public synchronized MemoryMetricsCollectorWithProcFile setPath(String path) {
    mPath = path;
    enable();
    return this;
  }

  @Override
  protected synchronized String getPath() {
    return mPath;
  }
}

class MemoryMetricsCollectorWithRuntime extends MemoryMetricsCollector {

  private Runtime mRuntime;

  public synchronized MemoryMetricsCollectorWithRuntime setRuntime(Runtime runtime) {
    mRuntime = runtime;
    enable();
    return this;
  }

  @Override
  protected synchronized Runtime getRuntime() {
    return mRuntime;
  }
}
