/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.network

import com.facebook.battery.metrics.network.NetworkMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class NetworkMetricsReporter : SystemMetricsReporter<NetworkMetrics> {

  override fun reportTo(metrics: NetworkMetrics, event: SystemMetricsReporter.Event) {
    if (metrics.mobileBytesTx != 0L) {
      event.add(MOBILE_BYTES_TX, metrics.mobileBytesTx)
    }

    if (metrics.mobileBytesRx != 0L) {
      event.add(MOBILE_BYTES_RX, metrics.mobileBytesRx)
    }

    if (metrics.wifiBytesTx != 0L) {
      event.add(WIFI_BYTES_TX, metrics.wifiBytesTx)
    }

    if (metrics.wifiBytesRx != 0L) {
      event.add(WIFI_BYTES_RX, metrics.wifiBytesRx)
    }
  }

  companion object {
    const val MOBILE_BYTES_TX: String = "mobile_bytes_tx"
    const val MOBILE_BYTES_RX: String = "mobile_bytes_rx"
    const val WIFI_BYTES_TX: String = "wifi_bytes_tx"
    const val WIFI_BYTES_RX: String = "wifi_bytes_rx"
  }
}
