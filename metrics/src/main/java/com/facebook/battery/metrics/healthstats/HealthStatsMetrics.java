// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.healthstats;

import android.os.Build;
import android.os.health.HealthStats;
import android.os.health.PackageHealthStats;
import android.os.health.PidHealthStats;
import android.os.health.ProcessHealthStats;
import android.os.health.ServiceHealthStats;
import android.os.health.TimerStat;
import android.os.health.UidHealthStats;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.core.Utilities;
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
  }

  private static final String TAG = "HealthStatsMetrics";
  private static final SparseArray<String> sKeyNames = new SparseArray<>();

  public String dataType;

  final SparseArray<Long> measurement = new SparseArray<>();
  final SparseArray<TimerMetrics> timer = new SparseArray<>();
  final SparseArray<ArrayMap<String, Long>> measurements = new SparseArray<>();
  final SparseArray<ArrayMap<String, TimerMetrics>> timers = new SparseArray<>();
  final SparseArray<ArrayMap<String, HealthStatsMetrics>> stats = new SparseArray<>();

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

    if (b == null) {
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
        K key = b.keyAt(i);
        if (a.get(key) == null) {
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
      timerMetrics.count = timerMetricsA.count + op * timerMetricsB.count;
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
        value.put(bValue.keyAt(j), new TimerMetrics(bValue.valueAt(j)));
      }
      timers.append(b.timers.keyAt(i), value);
    }

    stats.clear();
    for (int i = 0; i < b.stats.size(); i++) {
      ArrayMap<String, HealthStatsMetrics> bValue = b.stats.valueAt(i);
      ArrayMap<String, HealthStatsMetrics> value = new ArrayMap<>();
      for (int j = 0; j < bValue.size(); j++) {
        value.put(bValue.keyAt(j), new HealthStatsMetrics(bValue.valueAt(j)));
      }
      stats.append(b.stats.keyAt(i), value);
    }

    return this;
  }

  public HealthStatsMetrics set(HealthStats healthStats) {
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
      for (Map.Entry<String, TimerStat> entry : healthStats.getTimers(key).entrySet()) {
        value.put(entry.getKey(), new TimerMetrics(entry.getValue()));
      }
      timers.put(key, value);
    }

    stats.clear();
    for (int i = 0; i < healthStats.getStatsKeyCount(); i++) {
      int key = healthStats.getStatsKeyAt(i);
      ArrayMap<String, HealthStatsMetrics> value = new ArrayMap<>();
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
    return sKeyNames.get(key, "Unknown");
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

  public JSONObject toJSONObject() throws JSONException {
    JSONObject output = new JSONObject();
    output.put("type", dataType);

    int measurementCount = measurement.size();
    if (measurementCount > 0) {
      JSONObject measurementObj = new JSONObject();
      for (int i = 0; i < measurementCount; i++) {
        measurementObj.put(getKeyName(measurement.keyAt(i)), measurement.valueAt(i));
      }
      output.put("measurement", measurementObj);
    }

    int timerCount = timer.size();
    if (timerCount > 0) {
      JSONObject timerObj = new JSONObject();
      for (int i = 0; i < timerCount; i++) {
        timerObj.put(
            getKeyName(timer.keyAt(i)),
            new JSONObject()
                .put("count", timer.valueAt(i).count)
                .put("time_ms", timer.valueAt(i).timeMs));
      }
      output.put("timer", timerObj);
    }

    int measurementsCount = measurements.size();
    if (measurementsCount > 0) {
      JSONObject measurementsObj = new JSONObject();
      for (int i = 0; i < measurementsCount; i++) {
        measurementsObj.put(
            getKeyName(measurements.keyAt(i)), toJSONObject(measurements.valueAt(i)));
      }
      output.put("measurements", measurementsObj);
    }

    int timersCount = timers.size();
    if (timersCount > 0) {
      JSONObject timersObj = new JSONObject();
      for (int i = 0; i < timersCount; i++) {
        timersObj.put(getKeyName(timers.keyAt(i)), toJSONObject(timers.valueAt(i)));
      }
      output.put("timers", timersObj);
    }

    int statsCount = stats.size();
    if (statsCount > 0) {
      JSONObject statsObj = new JSONObject();
      for (int i = 0; i < statsCount; i++) {
        statsObj.put(getKeyName(stats.keyAt(i)), toJSONObject(stats.valueAt(i)));
      }
      output.put("stats", statsObj);
    }

    return output;
  }

  private static <V> JSONObject toJSONObject(@Nullable ArrayMap<String, V> map)
      throws JSONException {
    JSONObject mapObj = new JSONObject();
    for (int i = 0, len = map == null ? 0 : map.size(); i < len; i++) {
      V value = map.valueAt(i);
      if (value instanceof TimerMetrics) {
        mapObj.put(
            map.keyAt(i),
            new JSONObject()
                .put("count", ((TimerMetrics) value).count)
                .put("time_ms", ((TimerMetrics) value).timeMs));
      } else if (value instanceof HealthStatsMetrics) {
        mapObj.put(map.keyAt(i), ((HealthStatsMetrics) value).toJSONObject());
      } else {
        mapObj.put(map.keyAt(i), map.valueAt(i));
      }
    }
    return mapObj;
  }

  @Override
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
