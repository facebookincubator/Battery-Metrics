/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.healthstats;

import static com.facebook.battery.metrics.healthstats.HealthStatsMetrics.OP_DIFF;
import static com.facebook.battery.metrics.healthstats.HealthStatsMetrics.OP_SUM;
import static org.assertj.core.api.Java6Assertions.assertThat;

import android.os.health.UidHealthStats;
import android.util.SparseArray;
import androidx.collection.ArrayMap;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HealthStatsMetricsTest {

  private static final String TEST_DATATYPE = "Test";

  @Test
  public void testEquals() {
    HealthStatsMetrics metricsA = new HealthStatsMetrics();
    HealthStatsMetrics metricsB = new HealthStatsMetrics();
    assertThat(metricsA).isEqualTo(metricsB);
  }

  @Test
  public void testInitializedEquals() throws Exception {
    HealthStatsMetrics metricsA = createTestMetrics();
    HealthStatsMetrics metricsB = createTestMetrics();
    assertThat(metricsA).isEqualTo(metricsB);
  }

  @Test
  public void testDefaultValues() { // and sanity check for null values
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    assertThat(metrics.dataType).isNullOrEmpty();
    assertThat(metrics.measurement.size()).isEqualTo(0);
    assertThat(metrics.measurements.size()).isEqualTo(0);
    assertThat(metrics.timer.size()).isEqualTo(0);
    assertThat(metrics.timers.size()).isEqualTo(0);
    assertThat(metrics.stats.size()).isEqualTo(0);
  }

  @Test
  public void testSetUninitialized() {
    HealthStatsMetrics initialized = createTestMetrics();
    HealthStatsMetrics uninitialized = new HealthStatsMetrics();
    uninitialized.set(initialized);
    assertThat(uninitialized).isEqualTo(initialized);
  }

  @Test
  public void testSetInitialized() throws Exception {
    HealthStatsMetrics initialized = createTestMetrics();
    HealthStatsMetrics uninitialized = new HealthStatsMetrics();
    initialized.set(uninitialized);
    assertThat(uninitialized).isEqualTo(initialized);
  }

  @Test
  public void testSumArrayMaps() {
    ArrayMap<String, Long> a = new ArrayMap<>();
    a.put("a", 1L);
    a.put("c", 2L);

    ArrayMap<String, Long> b = new ArrayMap<>();
    b.put("b", 1L);
    b.put("c", 3L);

    ArrayMap<String, Long> sum = HealthStatsMetrics.opArrayMaps(OP_SUM, a, b);
    assertThat(sum.get("a")).isEqualTo(1);
    assertThat(sum.get("b")).isEqualTo(1);
    assertThat(sum.get("c")).isEqualTo(5);
    assertThat(sum.size()).isEqualTo(3);
  }

  @Test
  public void testDiffArrayMaps() {
    ArrayMap<String, Long> a = new ArrayMap<>();
    a.put("a", 1L);
    a.put("c", 2L);

    ArrayMap<String, Long> b = new ArrayMap<>();
    b.put("b", 1L);
    b.put("c", 3L);

    ArrayMap<String, Long> sum = HealthStatsMetrics.opArrayMaps(OP_DIFF, a, b);
    assertThat(sum.get("a")).isEqualTo(1);
    assertThat(sum.get("c")).isEqualTo(-1);
    assertThat(sum.size()).isEqualTo(2);
  }

  @Test
  public void testSumSparseArrays() {
    SparseArray<Long> a = new SparseArray<>();
    a.put(10, 10L);
    a.put(30, 30L);

    SparseArray<Long> b = new SparseArray<>();
    b.put(10, 10L);
    b.put(20, 20L);

    SparseArray<Long> sum = new SparseArray<>();
    HealthStatsMetrics.op(OP_SUM, a, b, sum);

    assertThat(sum.get(10)).isEqualTo(20);
    assertThat(sum.get(20)).isEqualTo(20);
    assertThat(sum.get(30)).isEqualTo(30);
    assertThat(sum.size()).isEqualTo(3);
  }

  @Test
  public void testDiffSparseArrays() {
    SparseArray<Long> a = new SparseArray<>();
    a.put(10, 10L);
    a.put(30, 30L);

    SparseArray<Long> b = new SparseArray<>();
    b.put(10, 10L);
    b.put(20, 20L);

    SparseArray<Long> sum = new SparseArray<>();
    HealthStatsMetrics.op(OP_DIFF, a, b, sum);

    assertThat(sum.get(10)).isEqualTo(0);
    assertThat(sum.get(30)).isEqualTo(30);
    assertThat(sum.size()).isEqualTo(2);
  }

  @Test
  public void testSum() {
    HealthStatsMetrics a = createTestMetrics();
    HealthStatsMetrics b = createTestMetrics();
    HealthStatsMetrics sum = a.sum(b, null);
    HealthStatsMetrics expectedSum = new HealthStatsMetrics();
    expectedSum.dataType = TEST_DATATYPE;
    expectedSum.measurement.put(123, 2000L);
    expectedSum.measurements.put(234, new ArrayMap<String, Long>());
    expectedSum.measurements.get(234).put("measurements", 4000L);
    expectedSum.timer.put(345, new HealthStatsMetrics.TimerMetrics(10, 4000));
    ArrayMap<String, HealthStatsMetrics.TimerMetrics> timersValues = new ArrayMap<>();
    timersValues.put("timers", new HealthStatsMetrics.TimerMetrics(12, 6000));
    expectedSum.timers.put(456, timersValues);
    ArrayMap<String, HealthStatsMetrics> value = new ArrayMap<>();
    value.put("stats", new HealthStatsMetrics(expectedSum));
    expectedSum.stats.put(1234, value);
    assertThat(sum).isEqualTo(expectedSum);
  }

  @Test
  public void testDiff() {
    HealthStatsMetrics a = createTestMetrics();
    HealthStatsMetrics b = createTestMetrics();
    HealthStatsMetrics diff = a.diff(b, null);
    HealthStatsMetrics expectedDiff = new HealthStatsMetrics();
    expectedDiff.dataType = TEST_DATATYPE;
    expectedDiff.measurement.put(123, 0L);
    expectedDiff.measurements.put(234, new ArrayMap<String, Long>());
    expectedDiff.measurements.get(234).put("measurements", 0L);
    expectedDiff.timer.put(345, new HealthStatsMetrics.TimerMetrics(0, 0));
    ArrayMap<String, HealthStatsMetrics.TimerMetrics> timersValues = new ArrayMap<>();
    timersValues.put("timers", new HealthStatsMetrics.TimerMetrics(0, 0));
    expectedDiff.timers.put(456, timersValues);
    ArrayMap<String, HealthStatsMetrics> value = new ArrayMap<>();
    value.put("stats", new HealthStatsMetrics(expectedDiff));
    expectedDiff.stats.put(1234, value);
    assertThat(diff).isEqualTo(expectedDiff);
  }

  @Test
  public void testDiffWithReset() {
    HealthStatsMetrics a = createTestMetrics();
    a.measurement.put(UidHealthStats.MEASUREMENT_REALTIME_BATTERY_MS, 100L);

    HealthStatsMetrics b = createTestMetrics();
    b.measurement.put(UidHealthStats.MEASUREMENT_REALTIME_BATTERY_MS, 200L);

    HealthStatsMetrics output = a.diff(b, null);
    HealthStatsMetrics expectedOutput = createTestMetrics();
    expectedOutput.measurement.put(UidHealthStats.MEASUREMENT_REALTIME_BATTERY_MS, 100L);
    assertThat(output).isEqualTo(expectedOutput);
  }

  private HealthStatsMetrics createTestMetrics() {
    HealthStatsMetrics metrics = createTestMetricsWithoutStats();
    ArrayMap<String, HealthStatsMetrics> value = new ArrayMap<>();
    value.put("stats", createTestMetricsWithoutStats());
    metrics.stats.put(1234, value);
    return metrics;
  }

  private HealthStatsMetrics createTestMetricsWithoutStats() {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.dataType = TEST_DATATYPE;
    metrics.measurement.put(123, 1000L);
    metrics.measurements.put(234, new ArrayMap<String, Long>());
    metrics.measurements.get(234).put("measurements", 2000L);
    metrics.timer.put(345, new HealthStatsMetrics.TimerMetrics(5, 2000));
    ArrayMap<String, HealthStatsMetrics.TimerMetrics> timersValues = new ArrayMap<>();
    timersValues.put("timers", new HealthStatsMetrics.TimerMetrics(6, 3000));
    metrics.timers.put(456, timersValues);
    return metrics;
  }

  @Test
  public void datatypeToJSON() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.dataType = TEST_DATATYPE;

    JSONObject json = metrics.toJSONObject();
    assertThat(json.getString("type")).isEqualTo(TEST_DATATYPE);
  }

  @Test
  public void measurementToJSON() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.measurement.put(234, 345L);
    JSONObject json = metrics.toJSONObject();
    assertThat(json.getJSONObject("measurement").getLong("234")).isEqualTo(345L);
  }

  @Test
  public void timerToJSON() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.timer.put(123, new HealthStatsMetrics.TimerMetrics(1, 11));
    JSONObject json = metrics.toJSONObject();
    assertThat(json.getJSONObject("timer")).isNotNull();
    assertThat(json.getJSONObject("timer").getJSONObject("123").getInt("count")).isEqualTo(1);
    assertThat(json.getJSONObject("timer").getJSONObject("123").getLong("time_ms")).isEqualTo(11L);
  }

  @Test
  public void measurementsToJSON() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.measurements.put(234, new ArrayMap<String, Long>());
    metrics.measurements.get(234).put("abcd", 2000L);
    JSONObject json = metrics.toJSONObject();
    assertThat(json.getJSONObject("measurements").getJSONObject("234").getLong("abcd"))
        .isEqualTo(2000L);
  }

  @Test
  public void timersToJSON() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.timers.put(345, new ArrayMap<String, HealthStatsMetrics.TimerMetrics>());
    metrics.timers.get(345).put("val", new HealthStatsMetrics.TimerMetrics(23, 24));
    JSONObject json = metrics.toJSONObject();
    assertThat(
            json.getJSONObject("timers").getJSONObject("345").getJSONObject("val").getInt("count"))
        .isEqualTo(23);
    assertThat(
            json.getJSONObject("timers")
                .getJSONObject("345")
                .getJSONObject("val")
                .getInt("time_ms"))
        .isEqualTo(24);
  }

  @Test
  public void statsToJSON() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.stats.put(123, new ArrayMap<String, HealthStatsMetrics>());
    HealthStatsMetrics inner = new HealthStatsMetrics();
    inner.dataType = TEST_DATATYPE;
    metrics.stats.get(123).put("abc", inner);
    JSONObject json = metrics.toJSONObject();
    assertThat(json.getJSONObject("stats").getJSONObject("123").getJSONObject("abc").toString())
        .isEqualTo(inner.toJSONObject().toString());
  }

  @Test
  public void jsonConversionSkipsEmptyContainers() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    assertThat(metrics.toJSONObject().length()).isEqualTo(0);
  }

  @Test
  public void jsonConversionSkipsZeroMeasurement() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.measurement.put(123, 0L);
    assertThat(metrics.toJSONObject().length()).isEqualTo(0);
  }

  @Test
  public void jsonConversionSkipsZeroTimer() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.timer.put(123, new HealthStatsMetrics.TimerMetrics(0, 0));
    assertThat(metrics.toJSONObject().length()).isEqualTo(0);
  }

  @Test
  public void jsonConversionDoesNotSkipPartialZeroTimer() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.timer.put(123, new HealthStatsMetrics.TimerMetrics(0, 2));
    metrics.timer.put(234, new HealthStatsMetrics.TimerMetrics(2, 0));
    assertThat(metrics.toJSONObject().getJSONObject("timer").length()).isEqualTo(2);
  }

  @Test
  public void jsonConversionSkipsZeroMeasurements() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.measurements.put(234, new ArrayMap<String, Long>());
    metrics.measurements.get(234).put("abcd", 0L);
    assertThat(metrics.toJSONObject().length()).isEqualTo(0);
  }

  @Test
  public void jsonConversionSkipsZeroTimers() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.timers.put(345, new ArrayMap<String, HealthStatsMetrics.TimerMetrics>());
    metrics.timers.get(345).put("val", new HealthStatsMetrics.TimerMetrics(0, 0));
    assertThat(metrics.toJSONObject().length()).isEqualTo(0);
  }

  @Test
  public void jsonConversionDoesNotSkipPartialZeroTimers() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.timers.put(345, new ArrayMap<String, HealthStatsMetrics.TimerMetrics>());
    metrics.timers.get(345).put("val", new HealthStatsMetrics.TimerMetrics(0, 10));
    metrics.timers.get(345).put("val2", new HealthStatsMetrics.TimerMetrics(20, 0));
    assertThat(metrics.toJSONObject().getJSONObject("timers").getJSONObject("345").length())
        .isEqualTo(2);
  }

  @Test
  public void jsonConversionSkipsEmptyHealthStats() throws Exception {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.stats.put(123, new ArrayMap<String, HealthStatsMetrics>());
    HealthStatsMetrics inner = new HealthStatsMetrics();
    metrics.stats.get(123).put("abc", inner);
    assertThat(metrics.toJSONObject().length()).isEqualTo(0);
  }
}
