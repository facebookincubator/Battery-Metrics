/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.healthstats;

import android.os.Build;
import android.os.health.HealthStats;
import android.os.health.PackageHealthStats;
import android.os.health.PidHealthStats;
import android.os.health.ProcessHealthStats;
import android.os.health.ServiceHealthStats;
import android.os.health.TimerStat;
import android.os.health.UidHealthStats;
import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import androidx.collection.ArrayMap;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.core.Utilities;
import com.facebook.infer.annotation.Nullsafe;
import java.lang.reflect.Field;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class takes the {@link android.os.health.HealthStats} object created by {@link
 * android.os.health.SystemHealthManager} and converting it into a format supported by the battery
 * metrics library: making it possible to add; subtract; save and upload healthstats data
 * conveniently.
 *
 * <p>To do this, it basically copies out all the fields provided in a HealthStats object because
 * those aren't particularly accessible, including a custom wrapper around TimerStats because that
 * class doesn't implement equals/hashcode.
 *
 * <p>This isn't particularly efficient, and shouldn't be used as frequently as the other metrics
 * collectors. For my own sanity I waste a lot of memory on snapshots to avoid bugs; this can be
 * optimized a lot by reusing objects for the internals/using object pools as the need arises.
 */
@Nullsafe(Nullsafe.Mode.LOCAL)
@RequiresApi(api = Build.VERSION_CODES.N)
public class HealthStatsMetrics extends SystemMetrics<HealthStatsMetrics> {

  @VisibleForTesting static final int OP_SUM = 1;
  @VisibleForTesting static final int OP_DIFF = -1;

  /** An alternative to TimerStat that actually implements equals and hashcode correctly. */
  public static class TimerMetrics {
    public int count;
    public long timeMs;

    public TimerMetrics() {}

    public TimerMetrics(TimerMetrics b) {
      count = b.count;
      timeMs = b.timeMs;
    }

    public TimerMetrics(TimerStat value) {
      count = value.getCount();
      timeMs = value.getTime();
    }

    public TimerMetrics(int count, long timeMs) {
      this.count = count;
      this.timeMs = timeMs;
    }

    @Override
    // NULLSAFE_FIXME[Inconsistent Subclass Parameter Annotation]
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      TimerMetrics that = (TimerMetrics) o;

      if (count != that.count) return false;
      return timeMs == that.timeMs;
    }

    @Override
    public int hashCode() {
      int result = count;
      result = 31 * result + (int) (timeMs ^ (timeMs >>> 32));
      return result;
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject output = new JSONObject();
      output.put("count", count);
      output.put("time_ms", timeMs);
      return output;
    }
  }

  private static final String TAG = "HealthStatsMetrics";
  private static final SparseArray<String> sKeyNames = new SparseArray<>();

  // NULLSAFE_FIXME[Field Not Initialized]
  public String dataType;

  public final SparseArray<Long> measurement = new SparseArray<>();
  public final SparseArray<TimerMetrics> timer = new SparseArray<>();
  public final SparseArray<ArrayMap<String, Long>> measurements = new SparseArray<>();
  public final SparseArray<ArrayMap<String, TimerMetrics>> timers = new SparseArray<>();
  public final SparseArray<ArrayMap<String, HealthStatsMetrics>> stats = new SparseArray<>();

  public HealthStatsMetrics() {}

  public HealthStatsMetrics(HealthStats healthStats) {
    set(healthStats);
  }

  public HealthStatsMetrics(HealthStatsMetrics metrics) {
    set(metrics);
  }

  @Override
  public HealthStatsMetrics sum(
      @Nullable HealthStatsMetrics b, @Nullable HealthStatsMetrics output) {
    if (output == null) {
      output = new HealthStatsMetrics();
    }
    output.dataType = dataType;

    if (b == null) {
      output.set(this);
    } else if (!strEquals(b.dataType, dataType)) {
      throw new IllegalArgumentException(
          "Attempting to add different types of HealthStatMetrics: "
              + dataType
              + " and "
              + b.dataType);
    } else {
      op(OP_SUM, measurement, b.measurement, output.measurement);
      op(OP_SUM, measurements, b.measurements, output.measurements);
      op(OP_SUM, timer, b.timer, output.timer);
      op(OP_SUM, timers, b.timers, output.timers);
      op(OP_SUM, stats, b.stats, output.stats);
    }

    return output;
  }

  @Override
  public HealthStatsMetrics diff(
      @Nullable HealthStatsMetrics b, @Nullable HealthStatsMetrics output) {
    if (output == null) {
      output = new HealthStatsMetrics();
    }
    output.dataType = dataType;

    if (b == null || compareSnapshotAge(this, b) < 0 /* short circuit if healthstats reset */) {
      output.set(this);
    } else if (!strEquals(b.dataType, dataType)) {
      throw new IllegalArgumentException(
          "Attempting to subtract different types of HealthStatMetrics: "
              + dataType
              + " and "
              + b.dataType);
    } else {
      op(OP_DIFF, measurement, b.measurement, output.measurement);
      op(OP_DIFF, measurements, b.measurements, output.measurements);
      op(OP_DIFF, timer, b.timer, output.timer);
      op(OP_DIFF, timers, b.timers, output.timers);
      op(OP_DIFF, stats, b.stats, output.stats);
    }

    return output;
  }

  /** Checks the age difference of snapshots, similar to String comparisons. */
  private static long compareSnapshotAge(HealthStatsMetrics a, HealthStatsMetrics b) {
    // NULLSAFE_FIXME[Nullable Dereference]
    long aRealtimeBatteryMs = a.measurement.get(UidHealthStats.MEASUREMENT_REALTIME_BATTERY_MS, 0L);
    // NULLSAFE_FIXME[Nullable Dereference]
    long bRealtimeBatteryMs = b.measurement.get(UidHealthStats.MEASUREMENT_REALTIME_BATTERY_MS, 0L);
    return aRealtimeBatteryMs - bRealtimeBatteryMs;
  }

  @VisibleForTesting
  static <K> SparseArray<K> op(int op, SparseArray<K> a, SparseArray<K> b, SparseArray<K> output) {
    output.clear();

    for (int i = 0; i < a.size(); i++) {
      int aKey = a.keyAt(i);
      output.put(aKey, (K) opValues(op, a.valueAt(i), b.get(aKey)));
    }

    if (op == OP_SUM) {
      for (int i = 0; i < b.size(); i++) {
        int bKey = b.keyAt(i);
        if (a.get(bKey) == null) {
          output.put(bKey, b.valueAt(i));
        }
      }
    }

    return output;
  }

  /** Acts as a union of the maps and sums values when they overlap. */
  @VisibleForTesting
  static <K, V> ArrayMap<K, V> opArrayMaps(int op, ArrayMap<K, V> a, @Nullable ArrayMap<K, V> b) {
    int aSize = a.size();

    ArrayMap<K, V> output = new ArrayMap<>();
    for (int i = 0; i < aSize; i++) {
      K key = a.keyAt(i);
      V bValue = b == null ? null : b.get(key);
      output.put(key, bValue == null ? a.valueAt(i) : (V) opValues(op, a.valueAt(i), bValue));
    }

    if (op == OP_SUM) {
      int bSize = b == null ? 0 : b.size();
      for (int i = 0; i < bSize; i++) {
        // NULLSAFE_FIXME[Nullable Dereference]
        K key = b.keyAt(i);
        if (a.get(key) == null) {
          // NULLSAFE_FIXME[Nullable Dereference]
          output.put(key, b.valueAt(i));
        }
      }
    }

    return output;
  }

  /** Kind of a hack to avoid a lot of boilerplate; icky but it works */
  private static <V> Object opValues(int op, V a, @Nullable V b) {
    if (a instanceof Long) {
      return (Long) a + (b == null ? 0 : (op * (Long) b));
    }

    if (a instanceof TimerMetrics) {
      TimerMetrics timerMetricsA = (TimerMetrics) a;
      TimerMetrics timerMetricsB = (TimerMetrics) b;

      if (b == null) {
        return new TimerMetrics(timerMetricsA);
      }

      TimerMetrics timerMetrics = new TimerMetrics();
      // NULLSAFE_FIXME[Nullable Dereference]
      timerMetrics.count = timerMetricsA.count + op * timerMetricsB.count;
      // NULLSAFE_FIXME[Nullable Dereference]
      timerMetrics.timeMs = timerMetricsA.timeMs + op * timerMetricsB.timeMs;
      return timerMetrics;
    }

    if (a instanceof HealthStatsMetrics) {
      if (op == OP_SUM) {
        return ((HealthStatsMetrics) a).sum((HealthStatsMetrics) b, null);
      } else {
        return ((HealthStatsMetrics) a).diff((HealthStatsMetrics) b, null);
      }
    }

    if (a instanceof ArrayMap) {
      return opArrayMaps(op, (ArrayMap) a, (ArrayMap) b);
    }

    throw new IllegalArgumentException("Handling unsupported values");
  }

  @Override
  public HealthStatsMetrics set(HealthStatsMetrics b) {
    dataType = b.dataType;

    measurement.clear();
    for (int i = 0; i < b.measurement.size(); i++) {
      measurement.append(b.measurement.keyAt(i), b.measurement.valueAt(i));
    }

    timer.clear();
    for (int i = 0; i < b.timer.size(); i++) {
      timer.append(b.timer.keyAt(i), new TimerMetrics(b.timer.valueAt(i)));
    }

    measurements.clear();
    for (int i = 0; i < b.measurements.size(); i++) {
      ArrayMap<String, Long> value = new ArrayMap<>();
      value.putAll((Map<String, Long>) b.measurements.valueAt(i));
      measurements.append(b.measurements.keyAt(i), value);
    }

    timers.clear();
    for (int i = 0; i < b.timers.size(); i++) {
      ArrayMap<String, TimerMetrics> bValue = b.timers.valueAt(i);
      ArrayMap<String, TimerMetrics> value = new ArrayMap<>();
      for (int j = 0; j < bValue.size(); j++) {
        // NULLSAFE_FIXME[Parameter Not Nullable]
        value.put(bValue.keyAt(j), new TimerMetrics(bValue.valueAt(j)));
      }
      timers.append(b.timers.keyAt(i), value);
    }

    stats.clear();
    for (int i = 0; i < b.stats.size(); i++) {
      ArrayMap<String, HealthStatsMetrics> bValue = b.stats.valueAt(i);
      ArrayMap<String, HealthStatsMetrics> value = new ArrayMap<>();
      for (int j = 0; j < bValue.size(); j++) {
        // NULLSAFE_FIXME[Parameter Not Nullable]
        value.put(bValue.keyAt(j), new HealthStatsMetrics(bValue.valueAt(j)));
      }
      stats.append(b.stats.keyAt(i), value);
    }

    return this;
  }

  public HealthStatsMetrics set(HealthStats healthStats) {
    // NULLSAFE_FIXME[Not Vetted Third-Party]
    dataType = healthStats.getDataType();

    measurement.clear();
    for (int i = 0; i < healthStats.getMeasurementKeyCount(); i++) {
      int key = healthStats.getMeasurementKeyAt(i);
      measurement.put(key, healthStats.getMeasurement(key));
    }

    measurements.clear();
    for (int i = 0; i < healthStats.getMeasurementsKeyCount(); i++) {
      int key = healthStats.getMeasurementsKeyAt(i);
      ArrayMap<String, Long> value = new ArrayMap<>();
      // NULLSAFE_FIXME[Not Vetted Third-Party]
      for (Map.Entry<String, Long> entry : healthStats.getMeasurements(key).entrySet()) {
        value.put(entry.getKey(), entry.getValue());
      }
      measurements.put(key, value);
    }

    timer.clear();
    for (int i = 0; i < healthStats.getTimerKeyCount(); i++) {
      int key = healthStats.getTimerKeyAt(i);
      TimerMetrics value =
          new TimerMetrics(healthStats.getTimerCount(key), healthStats.getTimerTime(key));
      timer.put(key, value);
    }

    timers.clear();
    for (int i = 0; i < healthStats.getTimersKeyCount(); i++) {
      int key = healthStats.getTimersKeyAt(i);
      ArrayMap<String, TimerMetrics> value = new ArrayMap<>();
      // NULLSAFE_FIXME[Not Vetted Third-Party]
      for (Map.Entry<String, TimerStat> entry : healthStats.getTimers(key).entrySet()) {
        value.put(entry.getKey(), new TimerMetrics(entry.getValue()));
      }
      timers.put(key, value);
    }

    stats.clear();
    for (int i = 0; i < healthStats.getStatsKeyCount(); i++) {
      int key = healthStats.getStatsKeyAt(i);
      ArrayMap<String, HealthStatsMetrics> value = new ArrayMap<>();
      // NULLSAFE_FIXME[Not Vetted Third-Party]
      for (Map.Entry<String, HealthStats> entry : healthStats.getStats(key).entrySet()) {
        value.put(entry.getKey(), new HealthStatsMetrics(entry.getValue()));
      }
      stats.put(key, value);
    }

    return this;
  }

  @Override
  public String toString() {
    StringBuilder stringValue = new StringBuilder("HealthStatsMetrics {\n");
    try {
      stringValue.append(toJSONObject().toString(2));
    } catch (JSONException je) {
      stringValue.append("<error>");
      Log.e(TAG, "Unable to convert to string", je);
    }
    stringValue.append("\n}");
    return stringValue.toString();
  }

  public static String getKeyName(int key) {
    if (sKeyNames.size() == 0) {
      readKeyNames();
    }
    // NULLSAFE_FIXME[Return Not Nullable]
    return sKeyNames.get(key, String.valueOf(key));
  }

  private static void readKeyNames() {
    try {
      Class[] healthStatsClasses = {
        UidHealthStats.class,
        PidHealthStats.class,
        ProcessHealthStats.class,
        PackageHealthStats.class,
        ServiceHealthStats.class
      };
      Class annotationClass = Class.forName("android.os.health.HealthKeys$Constant");
      for (Class clazz : healthStatsClasses) {
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
          if (field.isAnnotationPresent(annotationClass)) {
            sKeyNames.put(field.getInt(null), field.getName());
          }
        }
      }
      return;
    } catch (IllegalAccessException iae) {
      SystemMetricsLogger.wtf(TAG, "Unable to read constant names", iae);
    } catch (ClassNotFoundException cnfe) {
      SystemMetricsLogger.wtf(TAG, "Unable to find constant annotation", cnfe);
    }

    // Mark as attempted and invalid
    sKeyNames.put(-1, "Unable to read");
  }

  /** Converts to a JSON representation, stripping empty values */
  public JSONObject toJSONObject() throws JSONException {
    JSONObject output = new JSONObject();
    output.put("type", dataType);
    addMeasurement(output);
    addTimer(output);
    addMeasurements(output);
    addTimers(output);
    addStats(output);
    return output;
  }

  private void addMeasurement(JSONObject output) throws JSONException {
    JSONObject measurementObj = new JSONObject();
    for (int i = 0, count = measurement.size(); i < count; i++) {
      long value = measurement.valueAt(i);
      if (value != 0) {
        measurementObj.put(getKeyName(measurement.keyAt(i)), value);
      }
    }

    if (measurementObj.length() > 0) {
      output.put("measurement", measurementObj);
    }
  }

  private void addTimer(JSONObject output) throws JSONException {
    JSONObject timerObj = new JSONObject();
    for (int i = 0, count = timer.size(); i < count; i++) {
      TimerMetrics value = timer.valueAt(i);
      if (value.count != 0 || value.timeMs != 0) {
        timerObj.put(getKeyName(timer.keyAt(i)), value.toJSONObject());
      }
    }
    if (timerObj.length() > 0) {
      output.put("timer", timerObj);
    }
  }

  private void addMeasurements(JSONObject output) throws JSONException {
    JSONObject measurementsObj = new JSONObject();
    for (int i = 0, count = measurements.size(); i < count; i++) {
      ArrayMap<String, Long> value = measurements.valueAt(i);
      JSONObject valueOutput = new JSONObject();
      for (int j = 0, valueSize = value.size(); j < valueSize; j++) {
        // NULLSAFE_FIXME[Nullable Dereference]
        long v = value.valueAt(j);
        if (v != 0) {
          valueOutput.put(value.keyAt(j), v);
        }
      }

      if (valueOutput.length() > 0) {
        measurementsObj.put(getKeyName(measurements.keyAt(i)), valueOutput);
      }
    }
    if (measurementsObj.length() > 0) {
      output.put("measurements", measurementsObj);
    }
  }

  private void addTimers(JSONObject output) throws JSONException {
    JSONObject timersObj = new JSONObject();
    for (int i = 0, count = timers.size(); i < count; i++) {
      JSONObject valueOutput = new JSONObject();
      ArrayMap<String, TimerMetrics> value = timers.valueAt(i);
      for (int j = 0, valueCount = value.size(); j < valueCount; j++) {
        TimerMetrics v = value.valueAt(j);
        // NULLSAFE_FIXME[Nullable Dereference]
        if (v.count != 0 || v.timeMs != 0) {
          // NULLSAFE_FIXME[Nullable Dereference]
          valueOutput.put(value.keyAt(j), v.toJSONObject());
        }
      }
      if (valueOutput.length() > 0) {
        timersObj.put(getKeyName(timers.keyAt(i)), valueOutput);
      }
    }
    if (timersObj.length() > 0) {
      output.put("timers", timersObj);
    }
  }

  private void addStats(JSONObject output) throws JSONException {
    JSONObject statsObj = new JSONObject();
    for (int i = 0, count = stats.size(); i < count; i++) {
      JSONObject valueOutput = new JSONObject();
      ArrayMap<String, HealthStatsMetrics> value = stats.valueAt(i);
      for (int j = 0, valueCount = value.size(); j < valueCount; j++) {
        // NULLSAFE_FIXME[Nullable Dereference]
        JSONObject v = value.valueAt(j).toJSONObject();
        if (v.length() > 0) {
          valueOutput.put(value.keyAt(j), v);
        }
      }
      if (valueOutput.length() > 0) {
        statsObj.put(getKeyName(stats.keyAt(i)), valueOutput);
      }
    }

    if (statsObj.length() > 0) {
      output.put("stats", statsObj);
    }
  }

  @Override
  // NULLSAFE_FIXME[Inconsistent Subclass Parameter Annotation]
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HealthStatsMetrics that = (HealthStatsMetrics) o;

    if (dataType != null ? !dataType.equals(that.dataType) : that.dataType != null) return false;

    return Utilities.sparseArrayEquals(measurement, that.measurement)
        && Utilities.sparseArrayEquals(measurements, that.measurements)
        && Utilities.sparseArrayEquals(timer, that.timer)
        && Utilities.sparseArrayEquals(timers, that.timers)
        && Utilities.sparseArrayEquals(stats, that.stats);
  }

  @Override
  public int hashCode() {
    int result = dataType != null ? dataType.hashCode() : 0;
    result = 31 * result + measurement.hashCode();
    result = 31 * result + timer.hashCode();
    result = 31 * result + measurements.hashCode();
    result = 31 * result + timers.hashCode();
    result = 31 * result + stats.hashCode();
    return result;
  }

  private static boolean strEquals(@Nullable String a, @Nullable String b) {
    return a == null ? b == null : a.equals(b);
  }
}
