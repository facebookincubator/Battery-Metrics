/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.sensor;

import android.util.SparseArray;
import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.Utilities;

public class SensorMetrics extends SystemMetrics<SensorMetrics> {

  public boolean isAttributionEnabled;

  public final Consumption total = new Consumption();
  public final SparseArray<Consumption> sensorConsumption = new SparseArray<>();

  public SensorMetrics() {
    this(false);
  }

  public SensorMetrics(boolean isAttributionEnabled) {
    this.isAttributionEnabled = isAttributionEnabled;
  }

  @Override
  public SensorMetrics sum(@Nullable SensorMetrics b, @Nullable SensorMetrics output) {
    if (output == null) {
      output = new SensorMetrics(isAttributionEnabled);
    }

    if (b == null) {
      output.set(this);
    } else {
      total.sum(b.total, output.total);

      if (output.isAttributionEnabled) {
        op(+1, sensorConsumption, b.sensorConsumption, output.sensorConsumption);
      }
    }

    return output;
  }

  @Override
  public SensorMetrics diff(@Nullable SensorMetrics b, @Nullable SensorMetrics output) {
    if (output == null) {
      output = new SensorMetrics(isAttributionEnabled);
    }

    if (b == null) {
      output.set(this);
    } else {
      total.diff(b.total, output.total);
      if (output.isAttributionEnabled) {
        op(-1, sensorConsumption, b.sensorConsumption, output.sensorConsumption);
      }
    }

    return output;
  }

  @Override
  public SensorMetrics set(SensorMetrics b) {
    total.set(b.total);

    if (isAttributionEnabled && b.isAttributionEnabled) {
      sensorConsumption.clear();
      for (int i = 0, l = b.sensorConsumption.size(); i < l; i++) {
        sensorConsumption.put(b.sensorConsumption.keyAt(i), b.sensorConsumption.valueAt(i));
      }
    }

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
    return isAttributionEnabled == that.isAttributionEnabled
        && total.equals(that.total)
        && Utilities.sparseArrayEquals(sensorConsumption, that.sensorConsumption);
  }

  @Override
  public int hashCode() {
    int result = (isAttributionEnabled ? 1 : 0);
    result = 31 * result + total.hashCode();
    result = 31 * result + sensorConsumption.hashCode();
    return result;
  }

  private static final Consumption ZERO = new Consumption();

  private void op(
      int sign,
      SparseArray<Consumption> a,
      SparseArray<Consumption> b,
      SparseArray<Consumption> output) {
    output.clear();

    for (int i = 0, l = a.size(); i < l; i++) {
      int key = a.keyAt(i);
      Consumption result;
      if (sign > 0) {
        result = a.valueAt(i).sum(b.get(key, ZERO));
      } else {
        result = a.valueAt(i).diff(b.get(key, ZERO));
      }

      if (!ZERO.equals(result)) {
        output.put(key, result);
      }
    }

    for (int j = 0, l = b.size(); j < l; j++) {
      int key = b.keyAt(j);
      if (a.get(key) == null) {
        Consumption result;
        if (sign > 0) {
          result = ZERO.sum(b.valueAt(j));
        } else {
          result = ZERO.diff(b.valueAt(j));
        }

        if (!ZERO.equals(result)) {
          output.put(key, result);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "SensorMetrics{"
        + "isAttributionEnabled="
        + isAttributionEnabled
        + ", total="
        + total
        + ", sensorConsumption="
        + sensorConsumption
        + '}';
  }

  public static class Consumption extends SystemMetrics<Consumption> {
    public double powerMah;
    public long activeTimeMs;
    public long wakeUpTimeMs;

    public Consumption() {}

    public Consumption(double powerMah, long activeTimeMs, long wakeUpTimeMs) {
      this.powerMah = powerMah;
      this.activeTimeMs = activeTimeMs;
      this.wakeUpTimeMs = wakeUpTimeMs;
    }

    @Override
    public Consumption sum(@Nullable Consumption b, @Nullable Consumption output) {
      if (output == null) {
        output = new Consumption();
      }

      if (b == null) {
        output.set(this);
      } else {
        output.powerMah = b.powerMah + powerMah;
        output.activeTimeMs = b.activeTimeMs + activeTimeMs;
        output.wakeUpTimeMs = b.wakeUpTimeMs + wakeUpTimeMs;
      }

      return output;
    }

    @Override
    public Consumption diff(@Nullable Consumption b, @Nullable Consumption output) {
      if (output == null) {
        output = new Consumption();
      }

      if (b == null) {
        output.set(this);
      } else {
        output.powerMah = powerMah - b.powerMah;
        output.activeTimeMs = activeTimeMs - b.activeTimeMs;
        output.wakeUpTimeMs = wakeUpTimeMs - b.wakeUpTimeMs;
      }

      return output;
    }

    @Override
    public Consumption set(Consumption b) {
      powerMah = b.powerMah;
      activeTimeMs = b.activeTimeMs;
      wakeUpTimeMs = b.wakeUpTimeMs;
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

      Consumption that = (Consumption) o;
      return Double.compare(that.powerMah, powerMah) == 0
          && activeTimeMs == that.activeTimeMs
          && wakeUpTimeMs == that.wakeUpTimeMs;
    }

    @Override
    public int hashCode() {
      int result;
      long temp;
      temp = Double.doubleToLongBits(powerMah);
      result = (int) (temp ^ (temp >>> 32));
      result = 31 * result + (int) (activeTimeMs ^ (activeTimeMs >>> 32));
      result = 31 * result + (int) (wakeUpTimeMs ^ (wakeUpTimeMs >>> 32));
      return result;
    }

    @Override
    public String toString() {
      return "Consumption{"
          + "powerMah="
          + powerMah
          + ", activeTimeMs="
          + activeTimeMs
          + ", wakeUpTimeMs="
          + wakeUpTimeMs
          + '}';
    }
  }
}
