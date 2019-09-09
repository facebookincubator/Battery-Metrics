/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.sensor;

import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;

public class SensorMetrics extends SystemMetrics<SensorMetrics> {

  public double totalPowerMah;
  public long totalActiveTimeMs;
  public long totalWakeUpTimeMs;

  @Override
  public SensorMetrics sum(@Nullable SensorMetrics b, @Nullable SensorMetrics output) {
    if (output == null) {
      output = new SensorMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.totalPowerMah = b.totalPowerMah + totalPowerMah;
      output.totalActiveTimeMs = b.totalActiveTimeMs + totalActiveTimeMs;
      output.totalWakeUpTimeMs = b.totalWakeUpTimeMs + totalWakeUpTimeMs;
    }

    return output;
  }

  @Override
  public SensorMetrics diff(@Nullable SensorMetrics b, @Nullable SensorMetrics output) {
    if (output == null) {
      output = new SensorMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.totalPowerMah = totalPowerMah - b.totalPowerMah;
      output.totalActiveTimeMs = totalActiveTimeMs - b.totalActiveTimeMs;
      output.totalWakeUpTimeMs = totalWakeUpTimeMs - b.totalWakeUpTimeMs;
    }

    return output;
  }

  @Override
  public SensorMetrics set(SensorMetrics b) {
    totalPowerMah = b.totalPowerMah;
    totalWakeUpTimeMs = b.totalWakeUpTimeMs;
    totalActiveTimeMs = b.totalActiveTimeMs;
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

    SensorMetrics that = (SensorMetrics) o;

    return Double.compare(that.totalPowerMah, totalPowerMah) == 0
        && totalActiveTimeMs == that.totalActiveTimeMs
        && totalWakeUpTimeMs == that.totalWakeUpTimeMs;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(totalPowerMah);
    result = (int) (temp ^ (temp >>> 32));
    result = 31 * result + (int) (totalActiveTimeMs ^ (totalActiveTimeMs >>> 32));
    result = 31 * result + (int) (totalWakeUpTimeMs ^ (totalWakeUpTimeMs >>> 32));
    return result;
  }
}
