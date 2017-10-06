// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.support.annotation.VisibleForTesting;

import com.facebook.battery.metrics.api.SystemMetricsLogger;

import static com.facebook.battery.metrics.network.NetworkMetricsCollector.MOBILE;
import static com.facebook.battery.metrics.network.NetworkMetricsCollector.RX;
import static com.facebook.battery.metrics.network.NetworkMetricsCollector.TX;
import static com.facebook.battery.metrics.network.NetworkMetricsCollector.WIFI;

/**
 * The QTagUid based bytes collector reads bytes from xt_qtaguid/stats.
 *
 * The Quota-Tagging-UID module tracks network traffic by mapping each socket to the UID of the
 * owning application. There' a good description of why the kernel module was introduced at
 * https://fburl.com/1z1bd76x .
 *
 * An example snippet:
 * idx iface acct_tag_hex uid_tag_int cnt_set rx_bytes rx_packets tx_bytes tx_packets rx_tcp_bytes rx_tcp_packets rx_udp_bytes rx_udp_packets rx_other_bytes rx_other_packets tx_tcp_bytes tx_tcp_packets tx_udp_bytes tx_udp_packets tx_other_bytes tx_other_packets
 * 2 r_rmnet_data0 0x0 0 0 0 0 3744 56 0 0 0 0 0 0 0 0 0 0 3744 56
 * 3 r_rmnet_data0 0x0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 4 wlan0 0x0 0 0 18226861 31059 1490779 26793 16709350 21210 1477647 8826 39864 1023 1025429 19945 424802 6417 40548 431
 * 5 wlan0 0x0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 6 wlan0 0x0 1000 0 146348 660 172094 846 141940 645 4408 15 0 0 168694 833 3400 13 0 0
 * 7 wlan0 0x0 1000 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 8 wlan0 0x0 1020 0 726852 4285 171812 1536 0 0 726852 4285 0 0 0 0 171812 1536 0 0
 *
 * Note that the QTagUidNetworkBytesCollector assumes that all networks that are not wlan0, dummy0
 * and lo are mobile networks as a simplification to minimize state maintained within the collector.
 */
class QTagUidNetworkBytesCollector extends NetworkBytesCollector {
  private static final String TAG = "QTagUidNetworkBytesCollector";
  private static final String STATS_PATH = "/proc/net/xt_qtaguid/stats";
  private static final long UID = android.os.Process.myUid();

  private static final long WLAN0_HASH = "wlan0".hashCode();
  private static final long[] LOCAL_IFACE_HASHES = new long[] {
      "dummy0".hashCode(),
      "lo".hashCode(),
  };

  private RandomAccessFile mQTagUidStatsFile;
  private boolean mIsValid = true;
  private boolean mReachedEof = false;
  private boolean mHasPeeked = false;
  private int mChar;

  @Override
  protected void finalize() throws Throwable {
    closeFile();
  }

  @Override
  public boolean getTotalBytes(long[] bytes) {
    if (!mIsValid) {
      return false;
    }

    Arrays.fill(bytes, 0);

    try {
      if (mQTagUidStatsFile == null) {
        mQTagUidStatsFile = openFile();
      }

      mReachedEof = false;
      mQTagUidStatsFile.seek(0);

      // Skip headers.
      skipPast('\n');

      while (!mReachedEof && mIsValid && peek()) {
        skipPast(' '); // idx
        int ifaceHash = readHash(); // interface
        skipPast(' '); // tag
        long uid = readNumber(); // uid

        boolean isWifi = ifaceHash == WLAN0_HASH;
        boolean isMobile = !isWifi && !isLocalInterface(ifaceHash);

        if (uid != UID || !(isWifi || isMobile)) { // can read other uids for old android versions
          skipPast('\n');
          continue;
        }

        skipPast(' '); // cnt_set
        int field = isWifi ? WIFI : MOBILE;
        bytes[field | RX] += readNumber();
        skipPast(' '); // rx_packets
        bytes[field | TX] += readNumber();
        skipPast('\n');
      }
    } catch (IOException ioe) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse file", ioe);
      closeFile();
    }

    return mIsValid;
  }

  @VisibleForTesting
  @SuppressLint("InstanceMethodCanBeStatic")
  protected RandomAccessFile openFile() throws FileNotFoundException {
    return new RandomAccessFile(STATS_PATH, "r");
  }

  private long readNumber() throws IOException {
    boolean complete = false;
    boolean triggered = false;

    long result = 0;
    while (!complete && read()) {
      if (Character.isDigit(mChar)) {
        result = result * 10 + (mChar - '0');
        triggered = true;
      } else {
        complete = true;
      }
    }

    softAssert(triggered);
    return result;
  }

  private int readHash() throws IOException {
    boolean complete = false;
    boolean triggered = false;

    int hash = 0;
    while (!complete && read()) {
      if (mChar != ' ') {
        hash = 31 * hash + mChar; // Based off string
        triggered = true;
      } else {
        complete = true;
      }
    }

    softAssert(triggered);
    return hash;
  }

  private void skipPast(char ch) throws IOException {
    boolean complete = false;
    while (!complete && read()) {
      if (mChar == ch) {
        complete = true;
      }
    }

    softAssert(complete);
  }

  private void closeFile() {
    mIsValid = false;
    if (mQTagUidStatsFile != null) {
      try {
        mQTagUidStatsFile.close();
      } catch (IOException _) {
        // Ignore
      }
    }
  }

  private boolean peek() throws IOException {
    read();
    mHasPeeked = true;
    return !mReachedEof;
  }

  private boolean read() throws IOException {
    if (mHasPeeked) {
      mHasPeeked = false;
      return !mReachedEof;
    }

    mChar = mQTagUidStatsFile.read();
    mReachedEof = mChar == -1;
    return !mReachedEof;
  }

  private boolean softAssert(boolean test) {
    mIsValid &= test;
    return mIsValid;
  }

  private static boolean isLocalInterface(int ifaceHash) {
    for (int i = 0; i < LOCAL_IFACE_HASHES.length; i++) {
      if (ifaceHash == LOCAL_IFACE_HASHES[i]) {
        return true;
      }
    }
    return false;
  }
}
