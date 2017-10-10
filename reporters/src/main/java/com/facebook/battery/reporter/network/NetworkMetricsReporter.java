/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.reporter.network;

import com.facebook.battery.metrics.network.NetworkMetrics;
import com.facebook.battery.reporter.api.SystemMetricsReporter;

public class NetworkMetricsReporter implements SystemMetricsReporter<NetworkMetrics> {

  public static final String MOBILE_BYTES_TX = "mobile_bytes_tx";
  public static final String MOBILE_BYTES_RX = "mobile_bytes_rx";
  public static final String WIFI_BYTES_TX = "wifi_bytes_tx";
  public static final String WIFI_BYTES_RX = "wifi_bytes_rx";

  public void reportTo(NetworkMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.mobileBytesTx != 0) {
      event.add(MOBILE_BYTES_TX, metrics.mobileBytesTx);
    }

    if (metrics.mobileBytesRx != 0) {
      event.add(MOBILE_BYTES_RX, metrics.mobileBytesRx);
    }

    if (metrics.wifiBytesTx != 0) {
      event.add(WIFI_BYTES_TX, metrics.wifiBytesTx);
    }

    if (metrics.wifiBytesRx != 0) {
      event.add(WIFI_BYTES_RX, metrics.wifiBytesRx);
    }
  }
}
