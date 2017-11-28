// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import static com.facebook.battery.metrics.network.NetworkBytesCollector.BG;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.FG;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.MOBILE;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.RX;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.TX;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.WIFI;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayDeque;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetworkBytesCollector.class})
public class NetworkMetricsCollectorTest {
  private NetworkMetricsCollector mMetricsCollector;
  private TestNetworkBytesCollector mBytesCollector;

  @Before
  public void setUp() {
    PowerMockito.mockStatic(NetworkBytesCollector.class);

    mBytesCollector = new TestNetworkBytesCollector();
    PowerMockito.when(NetworkBytesCollector.create(null)).thenReturn(mBytesCollector);
    PowerMockito.when(NetworkBytesCollector.createByteArray()).thenCallRealMethod();
    mMetricsCollector = new NetworkMetricsCollector(null);
  }

  @Test
  public void testWithoutBgDetection() {
    long[] bytes = new long[4];
    bytes[MOBILE | TX | FG] = 1;
    bytes[WIFI | RX | FG] = 2;
    mBytesCollector.yieldTotalBytes(bytes);

    NetworkMetrics metrics = mMetricsCollector.createMetrics();
    boolean hasMetrics = mMetricsCollector.getSnapshot(metrics);
    assertThat(hasMetrics).isTrue();

    NetworkMetrics expected = new NetworkMetrics();
    expected.mobileBytesTx = 1;
    expected.wifiBytesRx = 2;
    assertThat(metrics).isEqualTo(expected);
  }

  @Test
  public void testWithBgDetection() {
    long[] bytes = new long[8];
    bytes[MOBILE | TX | FG] = 1;
    bytes[WIFI | RX | FG] = 2;
    bytes[MOBILE | TX | BG] = 30;
    bytes[WIFI | RX | BG] = 40;
    mBytesCollector.yieldTotalBytes(bytes);

    NetworkMetrics metrics = mMetricsCollector.createMetrics();
    boolean hasMetrics = mMetricsCollector.getSnapshot(metrics);
    assertThat(hasMetrics).isTrue();

    NetworkMetrics expected = new NetworkMetrics();
    expected.mobileBytesTx = 31;
    expected.wifiBytesRx = 42;
    assertThat(metrics).isEqualTo(expected);
  }

  private static class TestNetworkBytesCollector extends NetworkBytesCollector {
    private final ArrayDeque<long[]> mNextBytes = new ArrayDeque<>();
    private boolean mSupportsBgDistinction;

    public void yieldTotalBytes(long[] bytes) {
      mSupportsBgDistinction = determineSupportsBgDistinction(bytes.length);
      mNextBytes.addLast(bytes);
    }

    private static boolean determineSupportsBgDistinction(int byteLength) {
      switch (byteLength) {
        case 8:
          return true;
        case 4:
          return false;
        default:
          throw new IllegalArgumentException("length=" + byteLength);
      }
    }

    @Override
    public boolean supportsBgDistinction() {
      return mSupportsBgDistinction;
    }

    @Override
    public boolean getTotalBytes(long[] bytes) {
      if (!mNextBytes.isEmpty()) {
        long[] next = mNextBytes.removeFirst();
        System.arraycopy(next, 0, bytes, 0, next.length);
        return true;
      }
      return false;
    }
  }
}
