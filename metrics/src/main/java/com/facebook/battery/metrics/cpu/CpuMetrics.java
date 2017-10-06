// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.cpu;

import android.support.annotation.Nullable;
import com.facebook.battery.metrics.api.SystemMetrics;

/**
 * Information about system and user cpu metrics
 */
public class CpuMetrics extends SystemMetrics<CpuMetrics> {

  private static final long serialVersionUID = 0;

  public double userTimeS;
  public double systemTimeS;
  public double childUserTimeS;
  public double childSystemTimeS;

  public CpuMetrics() {
  }

  @Override
  public CpuMetrics set(CpuMetrics metrics) {
    userTimeS = metrics.userTimeS;
    systemTimeS = metrics.systemTimeS;
    childUserTimeS = metrics.childUserTimeS;
    childSystemTimeS = metrics.childSystemTimeS;
    return this;
  }

  @Override
  public CpuMetrics sum(@Nullable CpuMetrics b, @Nullable CpuMetrics output) {
    if (output == null) {
      output = new CpuMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.systemTimeS = systemTimeS + b.systemTimeS;
      output.userTimeS = userTimeS + b.userTimeS;
      output.childSystemTimeS = childSystemTimeS + b.childSystemTimeS;
      output.childUserTimeS = childUserTimeS + b.childUserTimeS;
    }

    return output;
  }

  @Override
  public CpuMetrics diff(@Nullable CpuMetrics b, @Nullable CpuMetrics output) {
    if (output == null) {
      output = new CpuMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.systemTimeS = systemTimeS - b.systemTimeS;
      output.userTimeS = userTimeS - b.userTimeS;
      output.childSystemTimeS = childSystemTimeS - b.childSystemTimeS;
      output.childUserTimeS = childUserTimeS - b.childUserTimeS;
    }

    return output;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    CpuMetrics that = (CpuMetrics) other;

    return Double.compare(that.systemTimeS, systemTimeS) == 0 &&
      Double.compare(that.userTimeS, userTimeS) == 0 &&
      Double.compare(that.childSystemTimeS, childSystemTimeS) == 0 &&
      Double.compare(that.childUserTimeS, childUserTimeS) == 0;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(systemTimeS);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(userTimeS);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(childSystemTimeS);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(childUserTimeS);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "CpuMetrics{" +
        "userTimeS=" + userTimeS +
        ", systemTimeS=" + systemTimeS +
        ", childUserTimeS=" + childUserTimeS +
        ", childSystemTimeS=" + childSystemTimeS +
        '}';
  }
}
