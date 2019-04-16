/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.healthstats;

import androidx.collection.ArrayMap;
import com.facebook.battery.metrics.healthstats.HealthStatsMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import com.facebook.battery.serializer.core.SystemMetricsSerializerTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HealthStatsMetricsSerializerTest
    extends SystemMetricsSerializerTest<HealthStatsMetrics> {

  @Override
  protected Class<HealthStatsMetrics> getClazz() {
    return HealthStatsMetrics.class;
  }

  @Override
  protected SystemMetricsSerializer<HealthStatsMetrics> getSerializer() {
    return new HealthStatsMetricsSerializer();
  }

  @Override
  protected HealthStatsMetrics createInitializedInstance() {
    HealthStatsMetrics metrics = createTestMetricsWithoutStats();
    ArrayMap<String, HealthStatsMetrics> value = new ArrayMap<>();
    value.put("stats", createTestMetricsWithoutStats());
    value.put("moreStats", createTestMetricsWithoutStats());
    metrics.stats.put(1234, value);

    ArrayMap<String, HealthStatsMetrics> secondValue = new ArrayMap<>();
    value.put("stats_2", createTestMetricsWithoutStats());
    metrics.stats.put(3456, secondValue);

    return metrics;
  }

  private HealthStatsMetrics createTestMetricsWithoutStats() {
    HealthStatsMetrics metrics = new HealthStatsMetrics();
    metrics.dataType = "test";

    metrics.measurement.put(123, 1000L);
    metrics.measurement.put(345, 1001L);

    metrics.measurements.put(234, new ArrayMap<String, Long>());
    metrics.measurements.get(234).put("measurements", 2000L);
    metrics.measurements.put(345, new ArrayMap<String, Long>());
    metrics.measurements.get(345).put("measurements_second", 3000L);

    metrics.timer.put(345, new HealthStatsMetrics.TimerMetrics(5, 2000));
    metrics.timer.put(123, new HealthStatsMetrics.TimerMetrics(8, 9000));

    ArrayMap<String, HealthStatsMetrics.TimerMetrics> timersValues = new ArrayMap<>();
    timersValues.put("timers", new HealthStatsMetrics.TimerMetrics(6, 3000));
    metrics.timers.put(456, timersValues);

    ArrayMap<String, HealthStatsMetrics.TimerMetrics> secondTimers = new ArrayMap<>();
    timersValues.put("timers_two", new HealthStatsMetrics.TimerMetrics(7, 8000));
    metrics.timers.put(123, timersValues);

    return metrics;
  }
}
