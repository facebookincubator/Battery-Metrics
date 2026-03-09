/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.network

import com.facebook.battery.metrics.network.RadioStateMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class RadioStateReporter : SystemMetricsReporter<RadioStateMetrics> {

  override fun reportTo(metrics: RadioStateMetrics, event: SystemMetricsReporter.Event) {
    if (metrics.mobileHighPowerActiveS != 0L) {
      event.add(MOBILE_HIGH_POWER_ACTIVE_S, metrics.mobileHighPowerActiveS)
    }
    if (metrics.mobileLowPowerActiveS != 0L) {
      event.add(MOBILE_LOW_POWER_ACTIVE_S, metrics.mobileLowPowerActiveS)
    }
    if (metrics.mobileRadioWakeupCount.toLong() != 0L) {
      event.add(MOBILE_RADIO_WAKEUP_COUNT, metrics.mobileRadioWakeupCount)
    }
    if (metrics.wifiActiveS != 0L) {
      event.add(WIFI_ACTIVE_S, metrics.wifiActiveS)
    }
    if (metrics.wifiRadioWakeupCount.toLong() != 0L) {
      event.add(WIFI_RADIO_WAKEUP_COUNT, metrics.wifiRadioWakeupCount)
    }
  }

  companion object {
    const val MOBILE_HIGH_POWER_ACTIVE_S: String = "mobile_high_power_active_s"
    const val MOBILE_LOW_POWER_ACTIVE_S: String = "mobile_low_power_active_s"
    const val MOBILE_RADIO_WAKEUP_COUNT: String = "mobile_radio_wakeup_count"
    const val WIFI_ACTIVE_S: String = "wifi_active_s"
    const val WIFI_RADIO_WAKEUP_COUNT: String = "wifi_radio_wakeup_count"
  }
}
