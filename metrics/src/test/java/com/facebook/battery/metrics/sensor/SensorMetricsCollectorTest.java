/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.sensor;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import com.facebook.battery.metrics.core.ShadowSystemClock;
import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowSystemClock.class})
public class SensorMetricsCollectorTest
    extends SystemMetricsCollectorTest<SensorMetrics, SensorMetricsCollector> {

  Sensor sensor;
  SensorEventListener listener;
  SensorMetricsCollector collector = new SensorMetricsCollector();
  SensorMetrics metrics = new SensorMetrics();

  @Before
  public void setUp() {
    sensor = mock(Sensor.class);
    listener = mock(SensorEventListener.class);

    when(sensor.getType()).thenReturn(1337);
    when(sensor.getPower()).thenReturn(10.0f);
  }

  @Test
  public void test_blank() {
    collector.getSnapshot(metrics);
    assertThat(metrics.total.activeTimeMs).isEqualTo(0);
    assertThat(metrics.total.powerMah).isEqualTo(0);
    assertThat(metrics.total.wakeUpTimeMs).isEqualTo(0);
  }

  @Test
  public void test_disabled() {
    collector.disable();

    assertThat(collector.getSnapshot(metrics)).isFalse();
  }

  /**
   *
   *
   * <pre>
   * Time    = 01 .. 20 .. 50
   * Sensor  = [......]
   * </pre>
   */
  @Test
  public void test_single_sensor() {
    ShadowSystemClock.setElapsedRealtime(1);
    collector.register(listener, sensor);

    ShadowSystemClock.setElapsedRealtime(10);
    assertThat(collector.getSnapshot(metrics)).isTrue();

    assertThat(metrics.total.powerMah).isEqualTo((((double) sensor.getPower()) * 9) / 3600 / 1000);
    assertThat(metrics.total.wakeUpTimeMs).isEqualTo(0);
    assertThat(metrics.total.activeTimeMs).isEqualTo(9);

    ShadowSystemClock.setElapsedRealtime(20);
    assertThat(collector.getSnapshot(metrics)).isTrue();

    assertThat(metrics.total.powerMah).isEqualTo((((double) sensor.getPower()) * 19) / 3600 / 1000);
    assertThat(metrics.total.wakeUpTimeMs).isEqualTo(0);
    assertThat(metrics.total.activeTimeMs).isEqualTo(19);

    collector.unregister(listener, null);

    ShadowSystemClock.setElapsedRealtime(50);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(19);
  }

  /**
   *
   *
   * <pre>
   * Time               = 01 .. 10 .. 15 .. 20 .. 25
   * Sensor/Listener A  = [..................]
   * Sensor/Listener B  =       [......]
   * </pre>
   */
  @Test
  public void test_multiple_listeners_single_sensor() {
    SensorEventListener listenerB = mock(SensorEventListener.class);

    ShadowSystemClock.setElapsedRealtime(1);
    collector.register(listener, sensor);

    ShadowSystemClock.setElapsedRealtime(5);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(4);

    ShadowSystemClock.setElapsedRealtime(10);
    collector.register(listenerB, sensor);

    ShadowSystemClock.setElapsedRealtime(11);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(10);

    ShadowSystemClock.setElapsedRealtime(15);
    collector.unregister(listenerB, null);

    ShadowSystemClock.setElapsedRealtime(20);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(19);

    collector.unregister(listener, null);

    ShadowSystemClock.setElapsedRealtime(25);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(19);
  }

  /**
   *
   *
   * <pre>
   * Time               = 01 .. 10 .. 15 .. 20
   * Sensor A/Listener  = [............]
   * Sensor B/Listener  =       [......]
   * </pre>
   */
  @Test
  public void test_single_listener_multiple_sensors() {
    Sensor sensorB = mock(Sensor.class);
    when(sensorB.getPower()).thenReturn(20.0f);
    when(sensorB.getType()).thenReturn(23);

    ShadowSystemClock.setElapsedRealtime(1);
    collector.register(listener, sensor);

    ShadowSystemClock.setElapsedRealtime(10);
    collector.register(listener, sensorB);

    ShadowSystemClock.setElapsedRealtime(15);
    collector.unregister(listener, null);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(5 + 14);
    assertThat(metrics.total.powerMah)
        .isEqualTo((5 * 20.0) / 3600 / 1000 + (14 * 10.0) / 3600 / 1000);
  }

  /**
   *
   *
   * <pre>
   * Time                = 01 .. 10 .. 15 .. 20 .. 50
   * Sensor A/Listener A = [............]
   * Sensor B/Listener B =       [............]
   * </pre>
   */
  @Test
  public void test_multiple_listeners_sensors() {
    Sensor sensorB = mock(Sensor.class);
    SensorEventListener listenerB = mock(SensorEventListener.class);

    ShadowSystemClock.setElapsedRealtime(1);
    collector.register(listener, sensor);

    ShadowSystemClock.setElapsedRealtime(5);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(4);

    ShadowSystemClock.setElapsedRealtime(10);
    collector.register(listenerB, sensorB);

    ShadowSystemClock.setElapsedRealtime(13);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(12 + 3);

    ShadowSystemClock.setElapsedRealtime(15);
    collector.unregister(listener, null);

    ShadowSystemClock.setElapsedRealtime(18);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(14 + 8);

    ShadowSystemClock.setElapsedRealtime(20);
    collector.unregister(listenerB, null);

    ShadowSystemClock.setElapsedRealtime(50);
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(14 + 10);
  }

  /**
   *
   *
   * <pre>
   * Time                = 01 .. 10 .. 15 .. 20 .. 50
   * Sensor A/Listener A = [............]
   * Sensor B/Listener B =       [............]
   * </pre>
   */
  @Test
  public void test_attribution_snapshot() {
    Sensor sensorB = mock(Sensor.class);
    SensorEventListener listenerB = mock(SensorEventListener.class);

    when(sensorB.getType()).thenReturn(2);
    when(sensorB.getPower()).thenReturn(100.0f);

    ShadowSystemClock.setElapsedRealtime(1);
    collector.register(listener, sensor);

    ShadowSystemClock.setElapsedRealtime(10);
    collector.register(listenerB, sensorB);

    ShadowSystemClock.setElapsedRealtime(15);
    collector.unregister(listener, null);

    ShadowSystemClock.setElapsedRealtime(20);
    collector.unregister(listenerB, null);

    ShadowSystemClock.setElapsedRealtime(50);

    metrics.isAttributionEnabled = true;
    assertThat(collector.getSnapshot(metrics)).isTrue();
    assertThat(metrics.total.activeTimeMs).isEqualTo(14 + 10);
    assertThat(metrics.sensorConsumption.size()).isEqualTo(2);
    assertThat(metrics.sensorConsumption.get(1337).activeTimeMs).isEqualTo(14);
    assertThat(metrics.sensorConsumption.get(1337).powerMah)
        .isEqualTo((14.0 * (double) sensor.getPower()) / 3600 / 1000);
    assertThat(metrics.sensorConsumption.get(2).activeTimeMs).isEqualTo(10);
    assertThat(metrics.sensorConsumption.get(2).powerMah)
        .isEqualTo((10.0 * (double) sensorB.getPower()) / 3600 / 1000);
  }

  @Override
  protected Class<SensorMetricsCollector> getClazz() {
    return SensorMetricsCollector.class;
  }
}
