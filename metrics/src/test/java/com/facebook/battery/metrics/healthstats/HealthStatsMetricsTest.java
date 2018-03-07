// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.healthstats;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.support.v4.util.ArrayMap;
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
    assertThat(metrics.measurementKeys).hasSize(0);
    assertThat(metrics.measurementValues).hasSize(0);
    assertThat(metrics.measurementsKeys).hasSize(0);
    assertThat(metrics.measurementsValues).hasSize(0);
    assertThat(metrics.timerKeys).hasSize(0);
    assertThat(metrics.timerValues).hasSize(0);
    assertThat(metrics.timersKeys).hasSize(0);
    assertThat(metrics.timersValues).hasSize(0);
    assertThat(metrics.statsKeys).hasSize(0);
    assertThat(metrics.statsValues).hasSize(0);
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

    ArrayMap<String, Long> sum = HealthStatsMetrics.sumArrayMaps(a, b, null);
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

    ArrayMap<String, Long> sum = HealthStatsMetrics.diffArrayMaps(a, b, null);
    assertThat(sum.get("a")).isEqualTo(1);
    assertThat(sum.get("c")).isEqualTo(-1);
    assertThat(sum.size()).isEqualTo(2);
  }

  @Test
  public void testSum() {
    HealthStatsMetrics a = createTestMetrics();
    HealthStatsMetrics b = createTestMetrics();

    HealthStatsMetrics sum = a.sum(b, null);

    assertThat(sum.dataType).isEqualTo(TEST_DATATYPE);

    assertThat(sum.measurementKeys).isEqualTo(a.measurementKeys);
    assertThat(sum.measurementValues).hasSameSizeAs(a.measurementValues);
    assertThat(sum.measurementValues[0]).isEqualTo(a.measurementValues[0] + b.measurementValues[0]);

    assertThat(sum.timerKeys).isEqualTo(a.timerKeys);
    assertThat(sum.timerValues).hasSameSizeAs(a.timerValues);
    assertThat(sum.timerValues[0].count).isEqualTo(a.timerValues[0].count + b.timerValues[0].count);
    assertThat(sum.timerValues[0].timeMs)
        .isEqualTo(a.timerValues[0].timeMs + b.timerValues[0].timeMs);

    assertThat(sum.measurementsKeys).isEqualTo(a.measurementsKeys);
    assertThat(sum.measurementsValues).hasSameSizeAs(a.measurementsValues);
    assertThat(sum.measurementsValues[0].get("measurements"))
        .isEqualTo(
            a.measurementsValues[0].get("measurements")
                + b.measurementsValues[0].get("measurements"));

    assertThat(sum.timersKeys).isEqualTo(a.timersKeys);
    assertThat(sum.timersValues).hasSameSizeAs(a.timersValues);
    assertThat(sum.timersValues[0].get("timers"))
        .isEqualTo(new HealthStatsMetrics.TimerMetrics(200, 4000));

    assertThat(sum.statsKeys).isEqualTo(a.statsKeys);
    assertThat(sum.statsValues).hasSameSizeAs(a.statsValues);
    // Just checking a single value here instead of a deep recursive check as that's been
    // tested above already.
    assertThat(sum.statsValues[0].get("stats").measurementValues[0])
        .isEqualTo(
            a.statsValues[0].get("stats").measurementValues[0]
                + b.statsValues[0].get("stats").measurementValues[0]);
  }

  @Test
  public void testDiff() {
    HealthStatsMetrics a = createTestMetrics();
    HealthStatsMetrics b = createTestMetrics();

    HealthStatsMetrics diff = a.diff(b, null);

    assertThat(diff.dataType).isEqualTo(TEST_DATATYPE);

    assertThat(diff.measurementKeys).isEqualTo(a.measurementKeys);
    assertThat(diff.measurementValues).hasSameSizeAs(a.measurementValues);
    assertThat(diff.measurementValues[0]).isEqualTo(0);

    assertThat(diff.timerKeys).isEqualTo(a.timerKeys);
    assertThat(diff.timerValues).hasSameSizeAs(a.timerValues);
    assertThat(diff.timerValues[0].count).isEqualTo(0);
    assertThat(diff.timerValues[0].timeMs).isEqualTo(0);

    assertThat(diff.measurementsKeys).isEqualTo(a.measurementsKeys);
    assertThat(diff.measurementsValues).hasSameSizeAs(a.measurementsValues);
    assertThat(diff.measurementsValues[0].get("measurements")).isEqualTo(0);

    assertThat(diff.timersKeys).isEqualTo(a.timersKeys);
    assertThat(diff.timersValues).hasSameSizeAs(a.timersValues);
    assertThat(diff.timersValues[0].get("timers"))
        .isEqualTo(new HealthStatsMetrics.TimerMetrics(0, 0));

    assertThat(diff.statsKeys).isEqualTo(a.statsKeys);
    assertThat(diff.statsValues).hasSameSizeAs(a.statsValues);
    // Just checking a single value here instead of a deep recursive check as that's been
    // tested above already.
    assertThat(diff.statsValues[0].get("stats").measurementValues[0]).isEqualTo(0);
  }

  private HealthStatsMetrics createTestMetrics() {
    HealthStatsMetrics metrics = createTestMetricsWithoutStats();

    metrics.statsKeys = new int[] {5};
    metrics.statsValues = (ArrayMap<String, HealthStatsMetrics>[]) (new ArrayMap[1]);
    metrics.statsValues[0] = new ArrayMap<>();
    metrics.statsValues[0].put("stats", createTestMetricsWithoutStats());
    return metrics;
  }

  private HealthStatsMetrics createTestMetricsWithoutStats() {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.dataType = TEST_DATATYPE;

    metrics.measurementKeys = new int[] {1};
    metrics.measurementValues = new long[] {1000};

    metrics.measurementsKeys = new int[] {2};
    metrics.measurementsValues = (ArrayMap<String, Long>[]) (new ArrayMap[1]);
    metrics.measurementsValues[0] = new ArrayMap<>();
    metrics.measurementsValues[0].put("measurements", 2000L);

    metrics.timerKeys = new int[] {3};
    metrics.timerValues =
        new HealthStatsMetrics.TimerMetrics[] {new HealthStatsMetrics.TimerMetrics()};
    metrics.timerValues[0].count = 5;
    metrics.timerValues[0].timeMs = 2000;

    metrics.timersKeys = new int[] {4};
    metrics.timersValues = (ArrayMap<String, HealthStatsMetrics.TimerMetrics>[]) (new ArrayMap[1]);
    metrics.timersValues[0] = new ArrayMap<>();
    HealthStatsMetrics.TimerMetrics timer = new HealthStatsMetrics.TimerMetrics();
    timer.count = 100;
    timer.timeMs = 2000;
    metrics.timersValues[0].put("timers", timer);

    return metrics;
  }
}
