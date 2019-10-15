/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.disk;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.annotations.OkToExtend;
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
public class DiskMetricsCollectorTest
    extends SystemMetricsCollectorTest<DiskMetrics, DiskMetricsCollector> {

  TemporaryFolder mFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    mFolder.create();
  }

  @Test
  public void testBrokenFile() throws Exception {
    DiskMetricsCollectorWithProcFile collector =
        new DiskMetricsCollectorWithProcFile()
            .setPath(createFile("I am a weird android manufacturer"));

    DiskMetrics snapshot = new DiskMetrics();
    assertThat(collector.getSnapshot(snapshot)).isFalse();
  }

  @Test
  public void testRealProcfile() throws Exception {
    String stat =
        "rchar: 100\n"
            + "wchar: 101\n"
            + "syscr: 1000\n"
            + "syscw: 1001\n"
            + "read_bytes: 500\n"
            + "write_bytes: 501\n"
            + "cancelled_write_bytes: 10\n";

    DiskMetricsCollectorWithProcFile collector =
        new DiskMetricsCollectorWithProcFile().setPath(createFile(stat));

    DiskMetrics snapshot = new DiskMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();

    assertThat(snapshot.rcharBytes).isEqualTo(100);
    assertThat(snapshot.wcharBytes).isEqualTo(101);
    assertThat(snapshot.syscrCount).isEqualTo(1000);
    assertThat(snapshot.syscwCount).isEqualTo(1001);
    assertThat(snapshot.readBytes).isEqualTo(500);
    assertThat(snapshot.writeBytes).isEqualTo(501);
    assertThat(snapshot.cancelledWriteBytes).isEqualTo(10);
  }

  @Test
  public void testRealProcfileWithNegative() throws Exception {
    String stat =
        "rchar: 100\n"
            + "wchar: 101\n"
            + "syscr: 1000\n"
            + "syscw: 1001\n"
            + "read_bytes: 500\n"
            + "write_bytes: 501\n"
            + "cancelled_write_bytes: -1\n";

    DiskMetricsCollectorWithProcFile collector =
        new DiskMetricsCollectorWithProcFile().setPath(createFile(stat));

    DiskMetrics snapshot = new DiskMetrics();
    assertThat(collector.getSnapshot(snapshot)).isTrue();

    assertThat(snapshot.rcharBytes).isEqualTo(100);
    assertThat(snapshot.wcharBytes).isEqualTo(101);
    assertThat(snapshot.syscrCount).isEqualTo(1000);
    assertThat(snapshot.syscwCount).isEqualTo(1001);
    assertThat(snapshot.readBytes).isEqualTo(500);
    assertThat(snapshot.writeBytes).isEqualTo(501);
    assertThat(snapshot.cancelledWriteBytes).isEqualTo(-1);
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
  protected Class<DiskMetricsCollector> getClazz() {
    return DiskMetricsCollector.class;
  }
}

@OkToExtend
class DiskMetricsCollectorWithProcFile extends DiskMetricsCollector {
  private String mPath;

  public synchronized DiskMetricsCollectorWithProcFile setPath(String path) {
    mPath = path;
    return this;
  }

  @Override
  protected synchronized String getPath() {
    return mPath;
  }
}
