/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.network;

import static com.facebook.battery.metrics.network.NetworkBytesCollector.BG;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.FG;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.MOBILE;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.RX;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.TX;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.WIFI;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class QTagUidNetworkBytesCollectorTest {

  private static final String HEADERS = "idx iface acct_tag_hex uid_tag_int cnt_set rx_bytes rx_packets tx_bytes tx_packets rx_tcp_bytes rx_tcp_packets rx_udp_bytes rx_udp_packets rx_other_bytes rx_other_packets tx_tcp_bytes tx_tcp_packets tx_udp_bytes tx_udp_packets tx_other_bytes tx_other_packets";
  private static final String[] INDEXES = HEADERS.split(" ");

  TemporaryFolder mFolder = new TemporaryFolder();
  long[] mBytes = new long[8];

  @Before
  public void setup() throws IOException {
    mFolder.create();
  }

  @Test
  public void testEmpty() throws Exception {
    QTagUidNetworkBytesCollector collector = new TestableCollector()
        .setQTagUidStatsFile(createFile(""));
    boolean result = collector.getTotalBytes(mBytes);

    assertThat(result).isFalse();
  }

  @Test
  public void testBlank() throws Exception {
    QTagUidNetworkBytesCollector collector = new TestableCollector()
        .setQTagUidStatsFile(createFile(HEADERS + "\n"));
    boolean result = collector.getTotalBytes(mBytes);

    assertThat(result).isTrue();
    assertThat(mBytes).isEqualTo(new long[8]);
  }

  @Test
  public void testWifi() throws Exception {
    StringBuilder contents = new StringBuilder();
    contents.append(HEADERS).append("\n");
    contents.append(createEntry(new HashMap<String, String>() { {
      put("iface", "wlan0");
      put("cnt_set", "1");
      put("rx_bytes", "100");
      put("tx_bytes", "200");
    }}));
    contents.append("\n");

    QTagUidNetworkBytesCollector collector = new TestableCollector()
        .setQTagUidStatsFile(createFile(contents.toString()));

    assertThat(collector.getTotalBytes(mBytes)).isTrue();
    assertThat(mBytes[WIFI | TX | FG]).isEqualTo(200);
    assertThat(mBytes[WIFI | RX | FG]).isEqualTo(100);
    assertThat(mBytes[MOBILE | TX | FG]).isEqualTo(0);
    assertThat(mBytes[MOBILE | RX | FG]).isEqualTo(0);
  }

  @Test
  public void testMobile() throws Exception {
    StringBuilder contents = new StringBuilder();
    contents.append(HEADERS).append("\n");
    contents.append(createEntry(new HashMap<String, String>() { {
      put("iface", "rmnet0");
      put("cnt_set", "1");
      put("rx_bytes", "100");
      put("tx_bytes", "200");
    }}));
    contents.append("\n");

    QTagUidNetworkBytesCollector collector = new TestableCollector()
        .setQTagUidStatsFile(createFile(contents.toString()));

    assertThat(collector.getTotalBytes(mBytes)).isTrue();
    assertThat(mBytes[WIFI | TX | FG]).isEqualTo(0);
    assertThat(mBytes[WIFI | RX | FG]).isEqualTo(0);
    assertThat(mBytes[MOBILE | TX | FG]).isEqualTo(200);
    assertThat(mBytes[MOBILE | RX | FG]).isEqualTo(100);
  }

  @Test
  public void testUidChecks() throws Exception {
    StringBuilder contents = new StringBuilder();
    contents.append(HEADERS).append("\n");
    contents.append(createEntry(new HashMap<String, String>() { {
      put("uid_tag_int", "1");
      put("iface", "rmnet0");
      put("rx_bytes", "100");
      put("tx_bytes", "200");
    }}));
    contents.append("\n");

    QTagUidNetworkBytesCollector collector = new TestableCollector()
        .setQTagUidStatsFile(createFile(contents.toString()));

    assertThat(collector.getTotalBytes(mBytes)).isTrue();
    assertThat(mBytes).isEqualTo(new long[8]);
  }

  @Test
  public void testWhitelistedInterfaces() throws Exception {
    StringBuilder contents = new StringBuilder();
    contents.append(HEADERS).append("\n");
    contents.append(createEntry(new HashMap<String, String>() { {
      put("iface", "dummy0");
      put("rx_bytes", "100");
      put("tx_bytes", "200");
    }}));
    contents.append("\n");

    QTagUidNetworkBytesCollector collector = new TestableCollector()
        .setQTagUidStatsFile(createFile(contents.toString()));

    assertThat(collector.getTotalBytes(mBytes)).isTrue();
    assertThat(mBytes).isEqualTo(new long[8]);
  }

  @Test
  public void testCntSet() throws Exception {
    StringBuilder contents = new StringBuilder();
    contents.append(HEADERS).append("\n");
    contents.append(createEntry(new HashMap<String, String>() { {
      put("iface", "rmnet0");
      put("cnt_set", "1");
      put("rx_bytes", "100");
      put("tx_bytes", "200");
    }}));
    contents.append("\n");
    contents.append(createEntry(new HashMap<String, String>() { {
      put("iface", "rmnet0");
      put("cnt_set", "0");
      put("rx_bytes", "1");
      put("tx_bytes", "2");
    }}));
    contents.append("\n");

    QTagUidNetworkBytesCollector collector =
        new TestableCollector().setQTagUidStatsFile(createFile(contents.toString()));

    assertThat(collector.getTotalBytes(mBytes)).isTrue();

    long[] expected = new long[8];
    Arrays.fill(expected, 0);
    expected[MOBILE | TX | FG] = 200;
    expected[MOBILE | RX | FG] = 100;
    expected[MOBILE | TX | BG] = 2;
    expected[MOBILE | RX | BG] = 1;
    assertThat(mBytes).isEqualTo(expected);
  }

  private File createFile(String contents) throws IOException {
    File file = mFolder.newFile();
    FileOutputStream os = new FileOutputStream(file, false);
    os.write(contents.getBytes());
    return file;
  }

  private static String createEntry(Map<String, String> map) {
    StringBuilder builder = new StringBuilder();
    for (String header : INDEXES) {
      builder.append(map.containsKey(header) ? map.get(header) : "0");
      builder.append(' ');
    }
    return builder.substring(0, builder.length() - 1);
  }
}

class TestableCollector extends QTagUidNetworkBytesCollector {

  private File mFile;

  public TestableCollector setQTagUidStatsFile(File file) {
    mFile = file;
    return this;
  }

  @Override
  protected String getPath() {
    return mFile.getPath();
  }
}
