/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.healthstats;

import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.collection.ArrayMap;
import com.facebook.battery.metrics.healthstats.HealthStatsMetrics;
import com.facebook.battery.metrics.healthstats.HealthStatsMetrics.TimerMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.N)
public class HealthStatsMetricsSerializer extends SystemMetricsSerializer<HealthStatsMetrics> {

  private static final long serialVersionUID = -874523681867511420L;

  @Override
  public long getTag() {
    return serialVersionUID;
  }

  @Override
  public void serializeContents(HealthStatsMetrics metrics, DataOutput output) throws IOException {
    writeString(metrics.dataType, output);

    int measurementLength = metrics.measurement.size();
    output.writeInt(measurementLength);
    for (int i = 0; i < measurementLength; i++) {
      output.writeInt(metrics.measurement.keyAt(i));
      output.writeLong(metrics.measurement.valueAt(i));
    }

    int timerLength = metrics.timer.size();
    output.writeInt(timerLength);
    for (int i = 0; i < timerLength; i++) {
      output.writeInt(metrics.timer.keyAt(i));
      writeTimer(metrics.timer.valueAt(i), output);
    }

    int measurementsLength = metrics.measurements.size();
    output.writeInt(measurementsLength);
    for (int i = 0; i < measurementsLength; i++) {
      output.writeInt(metrics.measurements.keyAt(i));
      ArrayMap<String, Long> currentMeasurement = metrics.measurements.valueAt(i);
      int currentMeasurementLength = currentMeasurement.size();
      output.writeInt(currentMeasurementLength);
      for (int j = 0; j < currentMeasurementLength; j++) {
        writeString(currentMeasurement.keyAt(j), output);
        output.writeLong(currentMeasurement.valueAt(j));
      }
    }

    int timersLength = metrics.timers.size();
    output.writeInt(timersLength);
    for (int i = 0; i < timersLength; i++) {
      output.writeInt(metrics.timers.keyAt(i));
      ArrayMap<String, TimerMetrics> currentTimer = metrics.timers.valueAt(i);
      int currentTimerLength = currentTimer.size();
      output.writeInt(currentTimerLength);
      for (int j = 0; j < currentTimerLength; j++) {
        writeString(currentTimer.keyAt(j), output);
        writeTimer(currentTimer.valueAt(j), output);
      }
    }

    int statsLength = metrics.stats.size();
    output.writeInt(statsLength);
    for (int i = 0; i < statsLength; i++) {
      output.writeInt(metrics.stats.keyAt(i));
      ArrayMap<String, HealthStatsMetrics> currentStats = metrics.stats.valueAt(i);
      int currentStatsLength = currentStats.size();
      output.writeInt(currentStatsLength);
      for (int j = 0; j < currentStatsLength; j++) {
        writeString(currentStats.keyAt(j), output);
        serializeContents(currentStats.valueAt(j), output);
      }
    }
  }

  private static void writeTimer(TimerMetrics timer, DataOutput output) throws IOException {
    output.writeInt(timer.count);
    output.writeLong(timer.timeMs);
  }

  private static void writeString(@Nullable String str, DataOutput output) throws IOException {
    if (str == null) {
      output.writeInt(0);
    } else {
      output.writeInt(str.length());
      output.writeBytes(str);
    }
  }

  @Override
  public boolean deserializeContents(HealthStatsMetrics metrics, DataInput input)
      throws IOException {
    metrics.dataType = readString(input);

    int measurementLength = input.readInt();
    for (int i = 0; i < measurementLength; i++) {
      metrics.measurement.put(input.readInt(), input.readLong());
    }

    int timerLength = input.readInt();
    for (int i = 0; i < timerLength; i++) {
      metrics.timer.put(input.readInt(), readTimer(input));
    }

    int measurementsLength = input.readInt();
    for (int i = 0; i < measurementsLength; i++) {
      int currentMeasurementKey = input.readInt();
      int currentMeasurementLength = input.readInt();
      ArrayMap<String, Long> currentMeasurement = new ArrayMap<>(currentMeasurementLength);
      for (int j = 0; j < currentMeasurementLength; j++) {
        currentMeasurement.put(readString(input), input.readLong());
      }

      metrics.measurements.put(currentMeasurementKey, currentMeasurement);
    }

    int timersLength = input.readInt();
    for (int i = 0; i < timersLength; i++) {
      int currentTimerKey = input.readInt();
      int currentTimerLength = input.readInt();
      ArrayMap<String, TimerMetrics> currentTimer = new ArrayMap<>(currentTimerLength);
      for (int j = 0; j < currentTimerLength; j++) {
        int length = input.readInt();
        byte[] bytes = new byte[length];
        input.readFully(bytes, 0, length);
        String key = new String(bytes);
        currentTimer.put(key, readTimer(input));
      }

      metrics.timers.put(currentTimerKey, currentTimer);
    }

    int statsLength = input.readInt();
    for (int i = 0; i < statsLength; i++) {
      int currentStatsKey = input.readInt();
      int currentStatsLength = input.readInt();
      ArrayMap<String, HealthStatsMetrics> currentStats = new ArrayMap<>(currentStatsLength);
      for (int j = 0; j < currentStatsLength; j++) {
        String key = readString(input);
        HealthStatsMetrics healthStatsMetrics = new HealthStatsMetrics();
        deserializeContents(healthStatsMetrics, input);
        currentStats.put(key, healthStatsMetrics);
      }

      metrics.stats.put(currentStatsKey, currentStats);
    }

    return true;
  }

  private static TimerMetrics readTimer(DataInput input) throws IOException {
    return new TimerMetrics(input.readInt(), input.readLong());
  }

  private static @Nullable String readString(DataInput input) throws IOException {
    int length = input.readInt();
    if (length == 0) {
      return null;
    } else {
      byte[] bytes = new byte[length];
      input.readFully(bytes, 0, length);
      return new String(bytes);
    }
  }
}
