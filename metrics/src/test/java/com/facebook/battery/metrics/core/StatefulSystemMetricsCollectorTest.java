/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.core;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Sanity check swapping in {@link StatefulSystemMetricsCollector} */
@RunWith(RobolectricTestRunner.class)
public class StatefulSystemMetricsCollectorTest {

  @Test
  public void testGetLatestDiffAndReset() throws Exception {
    DummyMetricCollector collector = new DummyMetricCollector();
    collector.currentValue = 10;

    StatefulSystemMetricsCollector<DummyMetric, DummyMetricCollector> statefulCollector =
        new StatefulSystemMetricsCollector<>(collector);
    assertThat(statefulCollector.getLatestDiffAndReset().value).isEqualTo(0);

    collector.currentValue = 20;
    assertThat(statefulCollector.getLatestDiffAndReset().value).isEqualTo(10);
    assertThat(statefulCollector.getLatestDiffAndReset().value).isEqualTo(0);
  }

  @Test
  public void testCustomBaseSnapshot() throws Exception {
    DummyMetric metric = new DummyMetric();
    metric.value = 0;

    DummyMetricCollector collector = new DummyMetricCollector();
    collector.currentValue = 10;

    StatefulSystemMetricsCollector<DummyMetric, DummyMetricCollector> withCustomInitialSnapshot =
        new StatefulSystemMetricsCollector<>(
            collector, collector.createMetrics(), metric, collector.createMetrics());
    StatefulSystemMetricsCollector<DummyMetric, DummyMetricCollector> defaultInitialSnapshot =
        new StatefulSystemMetricsCollector<>(collector);

    assertThat(withCustomInitialSnapshot.getLatestDiff().value).isEqualTo(10);
    assertThat(defaultInitialSnapshot.getLatestDiff().value).isEqualTo(0);
  }

  @Test
  public void testGetLatestDiff() {
    DummyMetricCollector collector = new DummyMetricCollector();
    collector.currentValue = 10;

    StatefulSystemMetricsCollector<DummyMetric, DummyMetricCollector> statefulCollector =
        new StatefulSystemMetricsCollector<>(collector);
    assertThat(statefulCollector.getLatestDiff().value).isEqualTo(0);

    collector.currentValue = 20;
    assertThat(statefulCollector.getLatestDiff().value).isEqualTo(10);

    collector.currentValue = 30;
    assertThat(statefulCollector.getLatestDiff().value).isEqualTo(20);
  }
}

class DummyMetric extends SystemMetrics<DummyMetric> {

  int value;

  @Override
  public DummyMetric sum(DummyMetric b, DummyMetric output) {
    output.value = value + b.value;
    return output;
  }

  @Override
  public DummyMetric diff(DummyMetric b, DummyMetric output) {
    output.value = value - b.value;
    return output;
  }

  @Override
  public DummyMetric set(DummyMetric b) {
    value = b.value;
    return this;
  }
}

class DummyMetricCollector extends SystemMetricsCollector<DummyMetric> {

  int currentValue;

  @Override
  public boolean getSnapshot(DummyMetric snapshot) {
    snapshot.value = currentValue;
    return true;
  }

  @Override
  public DummyMetric createMetrics() {
    return new DummyMetric();
  }
}
