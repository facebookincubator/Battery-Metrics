/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.sensor;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.core.SystemMetricsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SensorMetricsTest extends SystemMetricsTest<SensorMetrics> {

  @Override
  protected Class<SensorMetrics> getClazz() {
    return SensorMetrics.class;
  }

  @Test
  public void testEquals() {
    assertThat(new SensorMetrics()).isEqualTo(new SensorMetrics());
    assertThat(createAttributedMetrics()).isEqualTo(createAttributedMetrics());
  }

  @Test
  public void testDefaults() {
    SensorMetrics metrics = new SensorMetrics();
    assertThat(metrics.isAttributionEnabled).isFalse();
    assertThat(metrics.sensorConsumption.size()).isEqualTo(0);
    assertThat(metrics.total).isEqualTo(new SensorMetrics.Consumption());
  }

  @Test
  public void testConsumptionDefaults() {
    SensorMetrics.Consumption consumption = new SensorMetrics.Consumption();
    assertThat(consumption.activeTimeMs).isEqualTo(0);
    assertThat(consumption.wakeUpTimeMs).isEqualTo(0);
    assertThat(consumption.powerMah).isEqualTo(0.0);
  }

  @Test
  public void testSet() {
    SensorMetrics metrics = new SensorMetrics(true);
    metrics.set(createAttributedMetrics());
    assertThat(metrics).isEqualTo(createAttributedMetrics());
  }

  @Test
  public void testUnattributedSet() {
    SensorMetrics metrics = new SensorMetrics();
    metrics.set(createAttributedMetrics());

    SensorMetrics comparisonMetrics = createAttributedMetrics();
    comparisonMetrics.isAttributionEnabled = false;
    comparisonMetrics.sensorConsumption.clear();

    assertThat(metrics).isEqualTo(comparisonMetrics);
  }

  @Test
  public void testAttributedSum() {
    SensorMetrics a = createAttributedMetrics();
    SensorMetrics b = createAttributedMetrics();
    SensorMetrics result = a.sum(b);

    assertThat(result.total.powerMah).isEqualTo(a.total.powerMah * 2);
    assertThat(result.total.wakeUpTimeMs).isEqualTo(a.total.wakeUpTimeMs * 2);
    assertThat(result.total.activeTimeMs).isEqualTo(a.total.activeTimeMs * 2);

    for (int i = 0, l = result.sensorConsumption.size(); i < l; i++) {
      int key = result.sensorConsumption.keyAt(i);
      SensorMetrics.Consumption value = result.sensorConsumption.valueAt(i);
      assertThat(value).isEqualTo(a.sensorConsumption.get(key).sum(b.sensorConsumption.get(key)));
    }
  }

  @Test
  public void testAttributedDiff() {
    SensorMetrics a = createAttributedMetrics();
    SensorMetrics b = createAttributedMetrics();
    SensorMetrics result = a.diff(b);

    assertThat(result).isEqualTo(new SensorMetrics(true));
  }

  private SensorMetrics createAttributedMetrics() {
    SensorMetrics metrics = new SensorMetrics(true);
    metrics.total.powerMah = 11.0;
    metrics.total.wakeUpTimeMs = 100;
    metrics.total.activeTimeMs = 50;

    metrics.sensorConsumption.put(1, new SensorMetrics.Consumption(5.0, 30, 20));
    metrics.sensorConsumption.put(2, new SensorMetrics.Consumption(6.0, 20, 80));
    return metrics;
  }
}
