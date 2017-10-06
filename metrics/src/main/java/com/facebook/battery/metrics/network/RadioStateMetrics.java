// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import android.support.annotation.Nullable;
import com.facebook.battery.metrics.api.SystemMetrics;

/** Entity that contains info about radio uptime. */
public class RadioStateMetrics extends SystemMetrics<RadioStateMetrics> {

  private static final long serialVersionUID = 0;

  public long mobileLowPowerActiveS;
  public long mobileHighPowerActiveS;
  public int mobileRadioWakeupCount;
  public long wifiActiveS;
  public int wifiRadioWakeupCount;

  @Override
  public RadioStateMetrics sum(@Nullable RadioStateMetrics b, @Nullable RadioStateMetrics output) {
    if (output == null) {
      output = new RadioStateMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      output.mobileLowPowerActiveS = mobileLowPowerActiveS + b.mobileLowPowerActiveS;
      output.mobileHighPowerActiveS = mobileHighPowerActiveS + b.mobileHighPowerActiveS;
      output.mobileRadioWakeupCount = mobileRadioWakeupCount + b.mobileRadioWakeupCount;
      output.wifiActiveS = wifiActiveS + b.wifiActiveS;
      output.wifiRadioWakeupCount = wifiRadioWakeupCount + b.wifiRadioWakeupCount;
    }
    return output;
  }

  @Override
  public RadioStateMetrics diff(@Nullable RadioStateMetrics b, @Nullable RadioStateMetrics output) {
    if (output == null) {
      output = new RadioStateMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      output.mobileLowPowerActiveS = mobileLowPowerActiveS - b.mobileLowPowerActiveS;
      output.mobileHighPowerActiveS = mobileHighPowerActiveS - b.mobileHighPowerActiveS;
      output.mobileRadioWakeupCount = mobileRadioWakeupCount - b.mobileRadioWakeupCount;
      output.wifiActiveS = wifiActiveS - b.wifiActiveS;
      output.wifiRadioWakeupCount = wifiRadioWakeupCount - b.wifiRadioWakeupCount;
    }
    return output;
  }

  @Override
  public RadioStateMetrics set(RadioStateMetrics b) {
    mobileLowPowerActiveS = b.mobileLowPowerActiveS;
    mobileHighPowerActiveS = b.mobileHighPowerActiveS;
    mobileRadioWakeupCount = b.mobileRadioWakeupCount;
    wifiActiveS = b.wifiActiveS;
    wifiRadioWakeupCount = b.wifiRadioWakeupCount;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RadioStateMetrics that = (RadioStateMetrics) o;

    return mobileLowPowerActiveS == that.mobileLowPowerActiveS
        && mobileHighPowerActiveS == that.mobileHighPowerActiveS
        && mobileRadioWakeupCount == that.mobileRadioWakeupCount
        && wifiActiveS == that.wifiActiveS
        && wifiRadioWakeupCount == that.wifiRadioWakeupCount;
  }

  @Override
  public int hashCode() {
    int result = (int) (mobileLowPowerActiveS ^ (mobileLowPowerActiveS >>> 32));
    result = 31 * result + (int) (mobileHighPowerActiveS ^ (mobileHighPowerActiveS >>> 32));
    result = 31 * result + mobileRadioWakeupCount;
    result = 31 * result + (int) (wifiActiveS ^ (wifiActiveS >>> 32));
    result = 31 * result + wifiRadioWakeupCount;
    return result;
  }

  @Override
  public String toString() {
    return "RadioStateMetrics{"
        + "mobileLowPowerActiveS="
        + mobileLowPowerActiveS
        + ", mobileHighPowerActiveS="
        + mobileHighPowerActiveS
        + ", mobileRadioWakeupCount="
        + mobileRadioWakeupCount
        + ", wifiActiveS="
        + wifiActiveS
        + ", wifiRadioWakeupCount="
        + wifiRadioWakeupCount
        + '}';
  }
}
