/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.network;

import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.infer.annotation.Nullsafe;

/**
 * Information about network metrics: bytes sent/received on mobile radio and WiFi, as well as radio
 * uptime.
 *
 * <p>For foreground/background app state distinction, use {@link EnhancedNetworkMetrics}
 */
@Nullsafe(Nullsafe.Mode.LOCAL)
public class NetworkMetrics extends SystemMetrics<NetworkMetrics> {

  public long mobileBytesTx;
  public long mobileBytesRx;
  public long wifiBytesTx;
  public long wifiBytesRx;

  public NetworkMetrics() {}

  @Override
  public NetworkMetrics sum(@Nullable NetworkMetrics b, @Nullable NetworkMetrics output) {
    if (output == null) {
      output = new NetworkMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      output.mobileBytesTx = mobileBytesTx + b.mobileBytesTx;
      output.mobileBytesRx = mobileBytesRx + b.mobileBytesRx;
      output.wifiBytesTx = wifiBytesTx + b.wifiBytesTx;
      output.wifiBytesRx = wifiBytesRx + b.wifiBytesRx;
    }

    return output;
  }

  @Override
  public NetworkMetrics diff(@Nullable NetworkMetrics b, @Nullable NetworkMetrics output) {
    if (output == null) {
      output = new NetworkMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      output.mobileBytesTx = mobileBytesTx - b.mobileBytesTx;
      output.mobileBytesRx = mobileBytesRx - b.mobileBytesRx;
      output.wifiBytesTx = wifiBytesTx - b.wifiBytesTx;
      output.wifiBytesRx = wifiBytesRx - b.wifiBytesRx;
    }

    return output;
  }

  @Override
  public NetworkMetrics set(NetworkMetrics b) {
    mobileBytesRx = b.mobileBytesRx;
    mobileBytesTx = b.mobileBytesTx;
    wifiBytesRx = b.wifiBytesRx;
    wifiBytesTx = b.wifiBytesTx;
    return this;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    NetworkMetrics that = (NetworkMetrics) other;

    return mobileBytesTx == that.mobileBytesTx
        && mobileBytesRx == that.mobileBytesRx
        && wifiBytesTx == that.wifiBytesTx
        && wifiBytesRx == that.wifiBytesRx;
  }

  @Override
  public int hashCode() {
    int result = (int) (mobileBytesTx ^ (mobileBytesTx >>> 32));
    result = 31 * result + (int) (mobileBytesRx ^ (mobileBytesRx >>> 32));
    result = 31 * result + (int) (wifiBytesTx ^ (wifiBytesTx >>> 32));
    result = 31 * result + (int) (wifiBytesRx ^ (wifiBytesRx >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "NetworkMetrics{"
        + "mobileBytesTx="
        + mobileBytesTx
        + ", mobileBytesRx="
        + mobileBytesRx
        + ", wifiBytesTx="
        + wifiBytesTx
        + ", wifiBytesRx="
        + wifiBytesRx
        + '}';
  }
}
