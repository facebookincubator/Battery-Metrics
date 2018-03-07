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
import java.lang.reflect.Field;
import java.util.Arrays;
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
 * those aren't particularly accessible; we do directly use {@link TimerStat} objects, we'll be
 * done; this also implies significant amount of boilerplate in this class.
 *
 * <p>This isn't particularly efficient, and shouldn't be used as frequently as the other metrics
 * collectors. For my own sanity I waste a lot of memory on snapshots to avoid bugs; this can be
 * optimized a lot by reusing objects for the internals.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class HealthStatsMetrics extends SystemMetrics<HealthStatsMetrics> {

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

  /** Prevents lots of null checks with default values */
  private static final int[] EMPTY_INT_ARRAY = new int[0];

  private static final long[] EMPTY_LONG_ARRAY = new long[0];
  private static final TimerMetrics[] EMPTY_TIMER_ARRAY = new TimerMetrics[0];
  private static final ArrayMap[] EMPTY_ARRAY_MAP = new ArrayMap[0];

  private static final String TAG = "HealthStatsMetrics";
  private static final SparseArray<String> sKeyNames = new SparseArray<>();

  public String dataType;

  int[] timerKeys = EMPTY_INT_ARRAY;
  TimerMetrics[] timerValues = EMPTY_TIMER_ARRAY;

  // Measurement fields
  int[] measurementKeys = EMPTY_INT_ARRAY;
  long[] measurementValues = EMPTY_LONG_ARRAY;

  // Stats fields
  int[] statsKeys = EMPTY_INT_ARRAY;
  ArrayMap<String, HealthStatsMetrics>[] statsValues = EMPTY_ARRAY_MAP;

  // Timers fields
  int[] timersKeys = EMPTY_INT_ARRAY;
  ArrayMap<String, TimerMetrics>[] timersValues = EMPTY_ARRAY_MAP;

  // Measurements fields
  int[] measurementsKeys = EMPTY_INT_ARRAY;
  ArrayMap<String, Long>[] measurementsValues = EMPTY_ARRAY_MAP;

  @Override
  public HealthStatsMetrics sum(
      @Nullable HealthStatsMetrics b, @Nullable HealthStatsMetrics output) {
    if (output == null) {
      output = new HealthStatsMetrics();
    }

    if (b == null) {
      output.set(this);
    } else if (!strEquals(b.dataType, dataType)) {
      throw new IllegalArgumentException(
          "Attempting to add different types of HealthStatMetrics: "
              + dataType
              + " and "
              + b.dataType);
    } else {
      output.dataType = dataType;
      ensureArrayLengths(output, this);

      for (int i = 0; i < measurementKeys.length; i++) {
        output.measurementKeys[i] = measurementKeys[i];
        output.measurementValues[i] = measurementValues[i] + b.measurementValues[i];
      }

      for (int i = 0; i < measurementsKeys.length; i++) {
        output.measurementsKeys[i] = measurementsKeys[i];
        output.measurementsValues[i] =
            sumArrayMaps(
                measurementsValues[i], b.measurementsValues[i], output.measurementsValues[i]);
      }

      for (int i = 0; i < timerKeys.length; i++) {
        output.timerKeys[i] = timerKeys[i];
        output.timerValues[i] = (TimerMetrics) sumValues(timerValues[i], b.timerValues[i]);
      }

      for (int i = 0; i < timersKeys.length; i++) {
        output.timersKeys[i] = timersKeys[i];
        output.timersValues[i] =
            sumArrayMaps(timersValues[i], b.timersValues[i], output.timersValues[i]);
      }

      for (int i = 0; i < statsKeys.length; i++) {
        output.statsKeys[i] = statsKeys[i];
        output.statsValues[i] =
            sumArrayMaps(statsValues[i], b.statsValues[i], output.statsValues[i]);
      }
    }

    return output;
  }

  /** Acts as a union of the maps and sums values when they overlap. */
  @VisibleForTesting
  static <K, V> ArrayMap<K, V> sumArrayMaps(
      ArrayMap<K, V> a, ArrayMap<K, V> b, ArrayMap<K, V> output) {
    int aSize = a.size();
    int bSize = b.size();

    if (output == null) {
      output = new ArrayMap<>();
    } else {
      output.clear();
    }

    for (int i = 0; i < aSize; i++) {
      K key = a.keyAt(i);
      V bValue = b.get(key);
      output.put(key, bValue == null ? a.valueAt(i) : (V) sumValues(a.valueAt(i), bValue));
    }

    for (int i = 0; i < bSize; i++) {
      K key = b.keyAt(i);
      if (a.get(key) == null) {
        output.put(key, b.valueAt(i));
      }
    }

    return output;
  }

  /** Kind of a hack to avoid a lot of boilerplate; icky but it works */
  private static <V> Object sumValues(V a, V b) {
    if (a instanceof Long) {
      return (Long) a + (Long) b;
    } else if (a instanceof TimerMetrics) {
      TimerMetrics timerMetrics = new TimerMetrics();
      TimerMetrics timerMetricsA = (TimerMetrics) a;
      TimerMetrics timerMetricsB = (TimerMetrics) b;
      timerMetrics.count =
          (timerMetricsA != null ? timerMetricsA.count : 0)
              + (timerMetricsB != null ? timerMetricsB.count : 0);
      timerMetrics.timeMs =
          (timerMetricsA != null ? timerMetricsA.timeMs : 0)
              + (timerMetricsB != null ? timerMetricsB.timeMs : 0);
      return timerMetrics;
    } else if (a instanceof HealthStatsMetrics) {
      return ((HealthStatsMetrics) a).sum((HealthStatsMetrics) b, null);
    }

    throw new IllegalArgumentException("Adding unsupported values");
  }

  @Override
  public HealthStatsMetrics diff(
      @Nullable HealthStatsMetrics b, @Nullable HealthStatsMetrics output) {
    if (output == null) {
      output = new HealthStatsMetrics();
    }

    if (b == null) {
      output.set(this);
    } else if (!strEquals(b.dataType, dataType)) {
      throw new IllegalArgumentException(
          "Attempting to subtract different types of HealthStatMetrics: "
              + dataType
              + " and "
              + b.dataType);
    } else {
      output.dataType = dataType;
      ensureArrayLengths(output, this);

      for (int i = 0; i < measurementKeys.length; i++) {
        output.measurementKeys[i] = measurementKeys[i];
        output.measurementValues[i] = measurementValues[i] - b.measurementValues[i];
      }

      for (int i = 0; i < measurementsKeys.length; i++) {
        output.measurementsKeys[i] = measurementsKeys[i];
        output.measurementsValues[i] =
            diffArrayMaps(
                measurementsValues[i], b.measurementsValues[i], output.measurementsValues[i]);
      }

      for (int i = 0; i < timerKeys.length; i++) {
        output.timerKeys[i] = timerKeys[i];
        output.timerValues[i] = (TimerMetrics) diffValues(timerValues[i], b.timerValues[i]);
      }

      for (int i = 0; i < timersKeys.length; i++) {
        output.timersKeys[i] = timersKeys[i];
        output.timersValues[i] =
            diffArrayMaps(timersValues[i], b.timersValues[i], output.timersValues[i]);
      }

      for (int i = 0; i < statsKeys.length; i++) {
        output.statsKeys[i] = statsKeys[i];
        output.statsValues[i] =
            diffArrayMaps(statsValues[i], b.statsValues[i], output.statsValues[i]);
      }
    }

    return output;
  }

  /** Both acts like an intersection of array sets and subtracts values. */
  @VisibleForTesting
  static <K, V> ArrayMap<K, V> diffArrayMaps(
      ArrayMap<K, V> a, ArrayMap<K, V> b, ArrayMap<K, V> output) {
    int aSize = a.size();
    if (output == null) {
      output = new ArrayMap<>();
    } else {
      output.clear();
    }

    for (int i = 0; i < aSize; i++) {
      K key = a.keyAt(i);
      V bValue = b.get(key);
      output.put(key, bValue == null ? a.valueAt(i) : (V) diffValues(a.valueAt(i), bValue));
    }

    return output;
  }

  /** Kind of a hack to avoid a lot of boilerplate; icky but it works */
  private static <V> Object diffValues(V a, V b) {
    if (a instanceof Long) {
      return (Long) a - (Long) b;
    } else if (a instanceof TimerMetrics) {
      TimerMetrics timerMetrics = new TimerMetrics();
      TimerMetrics timerMetricsA = (TimerMetrics) a;
      TimerMetrics timerMetricsB = (TimerMetrics) b;
      timerMetrics.count =
          (timerMetricsA != null ? timerMetricsA.count : 0)
              - (timerMetricsB != null ? timerMetricsB.count : 0);
      timerMetrics.timeMs =
          (timerMetricsA != null ? timerMetricsA.timeMs : 0)
              - (timerMetricsB != null ? timerMetricsB.timeMs : 0);
      return timerMetrics;
    } else if (a instanceof HealthStatsMetrics) {
      return ((HealthStatsMetrics) a).diff((HealthStatsMetrics) b, null);
    }

    throw new IllegalArgumentException("Subtracting unsupported values");
  }

  @Override
  public HealthStatsMetrics set(HealthStatsMetrics b) {
    dataType = b.dataType;
    ensureArrayLengths(this, b);

    System.arraycopy(b.measurementKeys, 0, measurementKeys, 0, b.measurementKeys.length);
    System.arraycopy(b.measurementValues, 0, measurementValues, 0, b.measurementValues.length);

    System.arraycopy(b.measurementsKeys, 0, measurementsKeys, 0, b.measurementsKeys.length);
    for (int i = 0, len = measurementsKeys.length; i < len; i++) {
      measurementsValues[i] = new ArrayMap<>(b.measurementsValues[i]);
    }

    System.arraycopy(b.timerKeys, 0, timerKeys, 0, b.timerKeys.length);
    for (int i = 0, len = timerKeys.length; i < len; i++) {
      timerValues[i] = new TimerMetrics(b.timerValues[i]);
    }

    System.arraycopy(b.timersKeys, 0, timersKeys, 0, b.timersKeys.length);
    for (int i = 0, len = timersValues.length; i < len; i++) {
      timersValues[i] = new ArrayMap<>(b.timersValues[i].size());
      for (int j = 0; j < b.timersValues[i].size(); j++) {
        TimerMetrics timerMetrics = new TimerMetrics(b.timersValues[i].valueAt(j));
        timersValues[i].put(b.timersValues[i].keyAt(j), timerMetrics);
      }
    }

    System.arraycopy(b.statsKeys, 0, statsKeys, 0, b.statsKeys.length);
    for (int i = 0, len = statsValues.length; i < len; i++) {
      statsValues[i] = new ArrayMap<>(b.statsValues[i].size());
      for (int j = 0; j < b.statsValues[i].size(); j++) {
        HealthStatsMetrics metrics = new HealthStatsMetrics();
        metrics.set(b.statsValues[i].valueAt(j));
        statsValues[i].put(b.statsValues[i].keyAt(j), metrics);
      }
    }

    return this;
  }

  public HealthStatsMetrics set(HealthStats healthStats) {
    dataType = healthStats.getDataType();
    int measurementCount = healthStats.getMeasurementKeyCount();
    int measurementsCount = healthStats.getMeasurementsKeyCount();
    int timerCount = healthStats.getTimerKeyCount();
    int timersCount = healthStats.getTimersKeyCount();
    int statsCount = healthStats.getStatsKeyCount();
    ensureArrayLengths(
        this, measurementCount, measurementsCount, timerCount, timersCount, statsCount);

    for (int i = 0; i < measurementCount; i++) {
      measurementKeys[i] = healthStats.getMeasurementKeyAt(i);
      measurementValues[i] = healthStats.getMeasurement(measurementKeys[i]);
    }

    for (int i = 0; i < measurementsCount; i++) {
      measurementsKeys[i] = healthStats.getMeasurementsKeyAt(i);

      if (measurementsValues[i] == null) {
        measurementsValues[i] = new ArrayMap<>();
      } else {
        measurementsValues[i].clear();
      }
      measurementsValues[i].putAll(healthStats.getMeasurements(measurementsKeys[i]));
    }

    for (int i = 0; i < timerCount; i++) {
      timerKeys[i] = healthStats.getTimerKeyAt(i);
      timerValues[i] = new TimerMetrics();
      timerValues[i].count = healthStats.getTimerCount(timerKeys[i]);
      timerValues[i].timeMs = healthStats.getTimerTime(timerKeys[i]);
    }

    for (int i = 0; i < timersCount; i++) {
      timersKeys[i] = healthStats.getTimersKeyAt(i);
      if (timersValues[i] == null) {
        timersValues[i] = new ArrayMap<>();
      } else {
        timersValues[i].clear();
      }

      Map<String, TimerStat> timers = healthStats.getTimers(timersKeys[i]);
      for (Map.Entry<String, TimerStat> timer : timers.entrySet()) {
        timersValues[i].put(timer.getKey(), new TimerMetrics(timer.getValue()));
      }
    }

    for (int i = 0; i < statsCount; i++) {
      statsKeys[i] = healthStats.getStatsKeyAt(i);
      Map<String, HealthStats> statsValue = healthStats.getStats(statsKeys[i]);
      if (statsValues[i] == null) {
        statsValues[i] = new ArrayMap<>();
      } else {
        statsValues[i].clear();
      }

      for (Map.Entry<String, HealthStats> stats : statsValue.entrySet()) {
        HealthStatsMetrics metrics = new HealthStatsMetrics();
        metrics.set(stats.getValue());
        statsValues[i].put(stats.getKey(), metrics);
      }
    }

    return this;
  }

  private static void ensureArrayLengths(
      HealthStatsMetrics metrics,
      int measurementCount,
      int measurementsCount,
      int timerCount,
      int timersCount,
      int statsCount) {
    metrics.measurementKeys = ensureArrayLength(metrics.measurementKeys, measurementCount);
    metrics.measurementValues = ensureArrayLength(metrics.measurementValues, measurementCount);

    metrics.measurementsKeys = ensureArrayLength(metrics.measurementsKeys, measurementsCount);
    metrics.measurementsValues = ensureArrayLength(metrics.measurementsValues, measurementsCount);

    metrics.timerKeys = ensureArrayLength(metrics.timerKeys, timerCount);
    metrics.timerValues = ensureArrayLength(metrics.timerValues, timerCount);

    metrics.timersKeys = ensureArrayLength(metrics.timersKeys, timersCount);
    metrics.timersValues = ensureArrayLength(metrics.timersValues, timersCount);

    metrics.statsKeys = ensureArrayLength(metrics.statsKeys, statsCount);
    metrics.statsValues = ensureArrayLength(metrics.statsValues, statsCount);
  }

  private static void ensureArrayLengths(
      HealthStatsMetrics metrics, HealthStatsMetrics sourceMetrics) {
    ensureArrayLengths(
        metrics,
        sourceMetrics.measurementKeys.length,
        sourceMetrics.measurementsKeys.length,
        sourceMetrics.timerKeys.length,
        sourceMetrics.timersKeys.length,
        sourceMetrics.statsKeys.length);
  }

  private static int[] ensureArrayLength(int[] array, int size) {
    if (size == 0) {
      return EMPTY_INT_ARRAY;
    }

    return array != null && array.length == size ? array : new int[size];
  }

  private static long[] ensureArrayLength(long[] array, int size) {
    if (size == 0) {
      return EMPTY_LONG_ARRAY;
    }

    return array != null && array.length == size ? array : new long[size];
  }

  private static TimerMetrics[] ensureArrayLength(TimerMetrics[] array, int size) {
    if (size == 0) {
      return EMPTY_TIMER_ARRAY;
    }

    return array != null && array.length == size ? array : new TimerMetrics[size];
  }

  private static <K, V> ArrayMap<K, V>[] ensureArrayLength(ArrayMap<K, V>[] array, int size) {
    if (size == 0) {
      return EMPTY_ARRAY_MAP;
    }

    return array != null && array.length == size ? array : new ArrayMap[size];
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

    int measurementCount = measurementKeys.length;
    if (measurementCount > 0) {
      JSONObject measurement = new JSONObject();
      for (int i = 0; i < measurementCount; i++) {
        measurement.put(getKeyName(measurementKeys[i]), measurementValues[i]);
      }
      output.put("measurement", measurement);
    }

    int timerCount = timerKeys.length;
    if (timerCount > 0) {
      JSONObject timer = new JSONObject();
      for (int i = 0; i < timerCount; i++) {
        timer.put(
            getKeyName(timerKeys[i]),
            new JSONObject()
                .put("count", timerValues[i].count)
                .put("time_ms", timerValues[i].timeMs));
      }
      output.put("timer", timer);
    }

    int measurementsCount = measurementsKeys.length;
    if (measurementsCount > 0) {
      JSONObject measurements = new JSONObject();
      for (int i = 0; i < measurementsCount; i++) {
        measurements.put(getKeyName(measurementsKeys[i]), toJSONObject(measurementsValues[i]));
      }
      output.put("measurements", measurements);
    }

    int timersCount = timersKeys.length;
    if (timersCount > 0) {
      JSONObject timers = new JSONObject();
      for (int i = 0; i < timersCount; i++) {
        timers.put(getKeyName(timersKeys[i]), toJSONObject(timersValues[i]));
      }
      output.put("timers", timers);
    }

    int statsCount = statsKeys.length;
    if (statsCount > 0) {
      JSONObject stats = new JSONObject();
      for (int i = 0; i < statsCount; i++) {
        stats.put(getKeyName(statsKeys[i]), toJSONObject(statsValues[i]));
      }
      output.put("stats", stats);
    }

    return output;
  }

  private <V> JSONObject toJSONObject(@Nullable ArrayMap<String, V> arrayMap) throws JSONException {
    JSONObject map = new JSONObject();
    for (int i = 0, len = arrayMap == null ? 0 : arrayMap.size(); i < len; i++) {
      V value = arrayMap.valueAt(i);
      if (value instanceof TimerMetrics) {
        map.put(
            arrayMap.keyAt(i),
            new JSONObject()
                .put("count", ((TimerMetrics) value).count)
                .put("time_ms", ((TimerMetrics) value).timeMs));
      } else if (value instanceof HealthStatsMetrics) {
        map.put(arrayMap.keyAt(i), ((HealthStatsMetrics) value).toJSONObject());
      } else {
        map.put(arrayMap.keyAt(i), arrayMap.valueAt(i));
      }
    }
    return map;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    HealthStatsMetrics that = (HealthStatsMetrics) o;
    return ((dataType == null && that.dataType == null) || dataType.equals(that.dataType))
        && Arrays.equals(timerKeys, that.timerKeys)
        && Arrays.equals(timerValues, that.timerValues)
        && Arrays.equals(measurementKeys, that.measurementKeys)
        && Arrays.equals(measurementValues, that.measurementValues)
        && Arrays.equals(statsKeys, that.statsKeys)
        && Arrays.equals(timersKeys, that.timersKeys)
        && Arrays.equals(measurementsKeys, that.measurementsKeys)
        && Arrays.equals(statsValues, that.statsValues)
        && Arrays.equals(timersValues, that.timersValues)
        && Arrays.equals(measurementsValues, that.measurementsValues);
  }

  @Override
  public int hashCode() {
    int result = dataType != null ? dataType.hashCode() : 0;
    result = 31 * result + Arrays.hashCode(timerKeys);
    result = 31 * result + Arrays.hashCode(timerValues);
    result = 31 * result + Arrays.hashCode(measurementKeys);
    result = 31 * result + Arrays.hashCode(measurementValues);
    result = 31 * result + Arrays.hashCode(statsKeys);
    result = 31 * result + Arrays.hashCode(statsValues);
    result = 31 * result + Arrays.hashCode(timersKeys);
    result = 31 * result + Arrays.hashCode(timersValues);
    result = 31 * result + Arrays.hashCode(measurementsKeys);
    result = 31 * result + Arrays.hashCode(measurementsValues);
    return result;
  }

  private static boolean strEquals(String a, String b) {
    return a == null ? b == null : a.equals(b);
  }
}
