/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.Utilities;
import com.facebook.infer.annotation.Nullsafe;
import com.facebook.infer.annotation.ThreadSafe;
import java.util.ArrayList;
import java.util.Iterator;

@Nullsafe(Nullsafe.Mode.LOCAL)
@ThreadSafe
public class SensorMetricsCollector extends SystemMetricsCollector<SensorMetrics> {

  private static class SensorListenerData {

    final SensorEventListener listener;
    final Sensor sensor;

    SensorListenerData(SensorEventListener listener, Sensor sensor) {
      this.listener = listener;
      this.sensor = sensor;
    }
  }

  private static class SensorData {
    long startTimeMs;
    int activeCount;

    public SensorData(long startTimeMs, int activeCount) {
      this.startTimeMs = startTimeMs;
      this.activeCount = activeCount;
    }
  }

  @GuardedBy("this")
  private volatile boolean mEnabled = true;

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @GuardedBy("this")
  private final ArrayList<SensorListenerData> mActiveSensorData = new ArrayList<>();

  @GuardedBy("this")
  private final SimpleArrayMap<Sensor, SensorData> mActiveSensors = new SimpleArrayMap<>();

  @GuardedBy("this")
  private final SensorMetrics mCumulativeMetrics = new SensorMetrics(true);

  public synchronized void disable() {
    mEnabled = false;
    mActiveSensors.clear();
  }

  public synchronized void register(SensorEventListener listener, Sensor sensor) {
    if (!mEnabled) {
      return;
    }

    SensorListenerData data = new SensorListenerData(listener, sensor);
    mActiveSensorData.add(data);
    SensorData currentCount = mActiveSensors.get(sensor);
    if (currentCount == null) {
      currentCount = new SensorData(SystemClock.elapsedRealtime(), 1);
      mActiveSensors.put(sensor, currentCount);
    } else {
      currentCount.activeCount++;
    }
  }

  public synchronized void unregister(SensorEventListener listener, @Nullable Sensor sensor) {
    if (!mEnabled) {
      return;
    }

    long currentTimeMs = SystemClock.elapsedRealtime();
    Iterator<SensorListenerData> iter = mActiveSensorData.iterator();
    while (iter.hasNext()) {
      SensorListenerData data = iter.next();

      if (listener != data.listener || (sensor != null && sensor != data.sensor)) {
        continue;
      }

      iter.remove();

      SensorData currentSensor = mActiveSensors.get(data.sensor);
      if (currentSensor == null || currentSensor.activeCount == 0) {
        // Spurious / extra call
        continue;
      } else if (currentSensor.activeCount > 1) {
        // No additional book-keeping required at the moment
        currentSensor.activeCount -= 1;
        continue;
      }

      // Adjust for sensor's consumption
      mActiveSensors.remove(data.sensor);

      int type = data.sensor.getType();

      SensorMetrics.Consumption consumption = mCumulativeMetrics.sensorConsumption.get(type, null);
      if (consumption == null) {
        consumption = new SensorMetrics.Consumption();
        mCumulativeMetrics.sensorConsumption.put(type, consumption);
      }

      long currentActiveTimeMs = currentTimeMs - currentSensor.startTimeMs;
      consumption.activeTimeMs += currentActiveTimeMs;
      mCumulativeMetrics.total.activeTimeMs += currentActiveTimeMs;

      double currentPowerMah = energyConsumedMah(data.sensor, currentActiveTimeMs);
      consumption.powerMah += currentPowerMah;
      mCumulativeMetrics.total.powerMah += currentPowerMah;

      if (Util.isWakeupSensor(data.sensor)) {
        consumption.wakeUpTimeMs += currentActiveTimeMs;
        mCumulativeMetrics.total.wakeUpTimeMs += currentActiveTimeMs;
      }
    }
  }

  @Override
  public synchronized boolean getSnapshot(SensorMetrics snapshot) {
    Utilities.checkNotNull(snapshot, "Null value passed to getSnapshot!");

    if (!mEnabled) {
      return false;
    }

    long currentTimeMs = SystemClock.elapsedRealtime();
    snapshot.set(mCumulativeMetrics);

    for (int i = 0, l = mActiveSensors.size(); i < l; i++) {
      Sensor sensor = mActiveSensors.keyAt(i);
      SensorData data = mActiveSensors.valueAt(i);

      if (data == null) {
        continue;
      }

      if (data.activeCount <= 0) {
        continue;
      }

      long sensorActiveTimeMs = currentTimeMs - data.startTimeMs;
      double sensorPowerMah = energyConsumedMah(sensor, sensorActiveTimeMs);
      snapshot.total.activeTimeMs += sensorActiveTimeMs;
      snapshot.total.powerMah += sensorPowerMah;

      boolean isWakeupSensor = Util.isWakeupSensor(sensor);
      if (isWakeupSensor) {
        snapshot.total.wakeUpTimeMs += sensorActiveTimeMs;
      }

      if (snapshot.isAttributionEnabled) {
        int type = sensor.getType();
        SensorMetrics.Consumption consumption = snapshot.sensorConsumption.get(type);
        if (consumption == null) {
          consumption = new SensorMetrics.Consumption();
          snapshot.sensorConsumption.put(type, consumption);
        }

        consumption.activeTimeMs += sensorActiveTimeMs;
        consumption.powerMah += sensorPowerMah;

        if (isWakeupSensor) {
          consumption.wakeUpTimeMs += sensorActiveTimeMs;
        }
      }
    }

    return true;
  }

  @Override
  public SensorMetrics createMetrics() {
    return new SensorMetrics();
  }

  private static double energyConsumedMah(Sensor sensor, long activeTimeMs) {
    double sensorPowerMa = sensor.getPower();
    return sensorPowerMa * activeTimeMs / 3600 / 1000;
  }

  private static class Util {
    static boolean isWakeupSensor(Sensor sensor) {
      return sensor.isWakeUpSensor();
    }
  }
}
