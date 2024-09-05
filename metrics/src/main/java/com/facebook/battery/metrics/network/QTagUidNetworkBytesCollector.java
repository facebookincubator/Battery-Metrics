/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.network;

import android.annotation.SuppressLint;
import androidx.annotation.VisibleForTesting;
import com.facebook.battery.metrics.core.ProcFileReader;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.infer.annotation.Nullsafe;
import java.nio.CharBuffer;
import java.util.Arrays;
import javax.annotation.Nullable;

/**
 * The QTagUid based bytes collector reads bytes from xt_qtaguid/stats.
 *
 * <p>The Quota-Tagging-UID module tracks network traffic by mapping each socket to the UID of the
 * owning application. There' a good description of why the kernel module was introduced at
 * https://fburl.com/1z1bd76x .
 *
 * <p>An example snippet: idx iface acct_tag_hex uid_tag_int cnt_set rx_bytes rx_packets tx_bytes
 * tx_packets rx_tcp_bytes rx_tcp_packets rx_udp_bytes rx_udp_packets rx_other_bytes
 * rx_other_packets tx_tcp_bytes tx_tcp_packets tx_udp_bytes tx_udp_packets tx_other_bytes
 * tx_other_packets 2 r_rmnet_data0 0x0 0 0 0 0 3744 56 0 0 0 0 0 0 0 0 0 0 3744 56 3 r_rmnet_data0
 * 0x0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 4 wlan0 0x0 0 0 18226861 31059 1490779 26793 16709350
 * 21210 1477647 8826 39864 1023 1025429 19945 424802 6417 40548 431 5 wlan0 0x0 0 1 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0 6 wlan0 0x0 1000 0 146348 660 172094 846 141940 645 4408 15 0 0 168694 833 3400
 * 13 0 0 7 wlan0 0x0 1000 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 8 wlan0 0x0 1020 0 726852 4285 171812
 * 1536 0 0 726852 4285 0 0 0 0 171812 1536 0 0
 *
 * <p>Note that the QTagUidNetworkBytesCollector assumes that all networks that are not wlan0,
 * dummy0 and lo are mobile networks as a simplification to minimize state maintained within the
 * collector.
 */
@Nullsafe(Nullsafe.Mode.LOCAL)
class QTagUidNetworkBytesCollector extends NetworkBytesCollector {
  private static final String TAG = "QTagUidNetworkBytesCollector";
  private static final String STATS_PATH = "/proc/net/xt_qtaguid/stats";
  private static final long UID = android.os.Process.myUid();
  private static final CharBuffer WIFI_IFACE = CharBuffer.wrap("wlan0");
  private static final CharBuffer DUMMY_IFACE = CharBuffer.wrap("dummy0");
  private static final CharBuffer LOOPBACK_IFACE = CharBuffer.wrap("lo");

  private final CharBuffer mBuffer = CharBuffer.allocate(128);
  @Nullable private ProcFileReader mProcFileReader;

  @Override
  public boolean supportsBgDistinction() {
    return true;
  }

  @Override
  public boolean getTotalBytes(long[] bytes) {
    try {
      if (mProcFileReader == null) {
        mProcFileReader = new ProcFileReader(getPath());
      }

      mProcFileReader.reset();

      if (!mProcFileReader.isValid() || !mProcFileReader.hasNext()) {
        return false;
      }

      Arrays.fill(bytes, 0);

      // Skip headers.
      mProcFileReader.skipLine();

      while (mProcFileReader.hasNext()) {
        mProcFileReader.skipSpaces(); // Skip over idx

        mProcFileReader.readWord(mBuffer); // iface

        mProcFileReader.skipSpaces(); // Skip over acct_tag_hex
        mProcFileReader.skipSpaces();

        long uid = mProcFileReader.readNumber(); // uid_tag_int
        mProcFileReader.skipSpaces();

        boolean isWifi = WIFI_IFACE.compareTo(mBuffer) == 0;
        boolean isMobile =
            !isWifi
                && DUMMY_IFACE.compareTo(mBuffer) != 0
                && LOOPBACK_IFACE.compareTo(mBuffer) != 0;

        if (uid != UID || !(isWifi || isMobile)) { // can read other uids for old android versions
          mProcFileReader.skipLine();
          continue;
        }

        long cntSet = mProcFileReader.readNumber();
        mProcFileReader.skipSpaces();

        int field = 0;
        field |= (isWifi ? WIFI : MOBILE);
        field |= (cntSet == 0 ? BG : FG);

        bytes[field | RX] += mProcFileReader.readNumber(); // rx_bytes
        mProcFileReader.skipSpaces();

        mProcFileReader.skipSpaces(); // Skip over rx_packets

        bytes[field | TX] += mProcFileReader.readNumber(); // tx_bytes
        mProcFileReader.skipLine();
      }
    } catch (ProcFileReader.ParseException pe) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse file", pe);
      return false;
    }

    return true;
  }

  @VisibleForTesting
  @SuppressLint("InstanceMethodCanBeStatic")
  protected String getPath() {
    return STATS_PATH;
  }
}
