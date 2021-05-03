/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.network;

import com.facebook.battery.metrics.network.RadioStateMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.infer.annotation.Nullsafe;

@Nullsafe(Nullsafe.Mode.LOCAL)
public class RadioStateReporter implements SystemMetricsReporter<RadioStateMetrics> {

  public static final String MOBILE_HIGH_POWER_ACTIVE_S = "mobile_high_power_active_s";
  public static final String MOBILE_LOW_POWER_ACTIVE_S = "mobile_low_power_active_s";
  public static final String MOBILE_RADIO_WAKEUP_COUNT = "mobile_radio_wakeup_count";
  public static final String WIFI_ACTIVE_S = "wifi_active_s";
  public static final String WIFI_RADIO_WAKEUP_COUNT = "wifi_radio_wakeup_count";

  @Override
  public void reportTo(RadioStateMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.mobileHighPowerActiveS != 0L) {
      event.add(MOBILE_HIGH_POWER_ACTIVE_S, metrics.mobileHighPowerActiveS);
    }
    if (metrics.mobileLowPowerActiveS != 0L) {
      event.add(MOBILE_LOW_POWER_ACTIVE_S, metrics.mobileLowPowerActiveS);
    }
    if (metrics.mobileRadioWakeupCount != 0L) {
      event.add(MOBILE_RADIO_WAKEUP_COUNT, metrics.mobileRadioWakeupCount);
    }
    if (metrics.wifiActiveS != 0L) {
      event.add(WIFI_ACTIVE_S, metrics.wifiActiveS);
    }
    if (metrics.wifiRadioWakeupCount != 0L) {
      event.add(WIFI_RADIO_WAKEUP_COUNT, metrics.wifiRadioWakeupCount);
    }
  }
}
