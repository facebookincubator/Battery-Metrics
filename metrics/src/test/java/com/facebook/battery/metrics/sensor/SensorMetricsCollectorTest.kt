/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.sensor

import android.hardware.Sensor
import android.hardware.SensorEventListener
import com.facebook.battery.metrics.core.ShadowSystemClock
import com.facebook.battery.metrics.core.SystemMetricsCollectorTest
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [SensorMetricsCollector].
 *
 * Tests validate sensor registration, unregistration, and metrics collection for single and
 * multiple sensors with various listener configurations.
 */
@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowSystemClock::class])
class SensorMetricsCollectorTest :
    SystemMetricsCollectorTest<SensorMetrics, SensorMetricsCollector>() {

  private lateinit var sensor: Sensor
  private lateinit var listener: SensorEventListener
  private val collector = SensorMetricsCollector()
  private val metrics = SensorMetrics()

  @Before
  fun setUp() {
    sensor = mock()
    listener = mock()

    whenever(sensor.type).thenReturn(1337)
    whenever(sensor.power).thenReturn(10.0f)
  }

  @Test
  fun test_blank() {
    collector.getSnapshot(metrics)
    assertThat(metrics.total.activeTimeMs).isEqualTo(0)
    assertThat(metrics.total.powerMah).isEqualTo(0)
    assertThat(metrics.total.wakeUpTimeMs).isEqualTo(0)
  }

  @Test
  fun test_disabled() {
    collector.disable()

    assertThat(collector.getSnapshot(metrics)).isFalse()
  }

  /**
   * ```
   * Time    = 01 .. 20 .. 50
   * Sensor  = [......]
   * ```
   */
  @Test
  fun test_single_sensor() {
    ShadowSystemClock.setElapsedRealtime(1)
    collector.register(listener, sensor)

    ShadowSystemClock.setElapsedRealtime(10)
    assertThat(collector.getSnapshot(metrics)).isTrue()

    assertThat(metrics.total.powerMah).isEqualTo(sensor.power.toDouble() * 9 / 3600 / 1000)
    assertThat(metrics.total.wakeUpTimeMs).isEqualTo(0)
    assertThat(metrics.total.activeTimeMs).isEqualTo(9)

    ShadowSystemClock.setElapsedRealtime(20)
    assertThat(collector.getSnapshot(metrics)).isTrue()

    assertThat(metrics.total.powerMah).isEqualTo(sensor.power.toDouble() * 19 / 3600 / 1000)
    assertThat(metrics.total.wakeUpTimeMs).isEqualTo(0)
    assertThat(metrics.total.activeTimeMs).isEqualTo(19)

    collector.unregister(listener, null)

    ShadowSystemClock.setElapsedRealtime(50)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(19)
  }

  /**
   * ```
   * Time               = 01 .. 10 .. 15 .. 20 .. 25
   * Sensor/Listener A  = [..................]
   * Sensor/Listener B  =       [......]
   * ```
   */
  @Test
  fun test_multiple_listeners_single_sensor() {
    val listenerB: SensorEventListener = mock()

    ShadowSystemClock.setElapsedRealtime(1)
    collector.register(listener, sensor)

    ShadowSystemClock.setElapsedRealtime(5)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(4)

    ShadowSystemClock.setElapsedRealtime(10)
    collector.register(listenerB, sensor)

    ShadowSystemClock.setElapsedRealtime(11)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(10)

    ShadowSystemClock.setElapsedRealtime(15)
    collector.unregister(listenerB, null)

    ShadowSystemClock.setElapsedRealtime(20)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(19)

    collector.unregister(listener, null)

    ShadowSystemClock.setElapsedRealtime(25)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(19)
  }

  /**
   * ```
   * Time               = 01 .. 10 .. 15 .. 20
   * Sensor A/Listener  = [............]
   * Sensor B/Listener  =       [......]
   * ```
   */
  @Test
  fun test_single_listener_multiple_sensors() {
    val sensorB: Sensor = mock()
    whenever(sensorB.power).thenReturn(20.0f)
    whenever(sensorB.type).thenReturn(23)

    ShadowSystemClock.setElapsedRealtime(1)
    collector.register(listener, sensor)

    ShadowSystemClock.setElapsedRealtime(10)
    collector.register(listener, sensorB)

    ShadowSystemClock.setElapsedRealtime(15)
    collector.unregister(listener, null)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(5 + 14)
    assertThat(metrics.total.powerMah).isEqualTo(5 * 20.0 / 3600 / 1000 + 14 * 10.0 / 3600 / 1000)
  }

  /**
   * ```
   * Time                = 01 .. 10 .. 15 .. 20 .. 50
   * Sensor A/Listener A = [............]
   * Sensor B/Listener B =       [............]
   * ```
   */
  @Test
  fun test_multiple_listeners_sensors() {
    val sensorB: Sensor = mock()
    val listenerB: SensorEventListener = mock()

    ShadowSystemClock.setElapsedRealtime(1)
    collector.register(listener, sensor)

    ShadowSystemClock.setElapsedRealtime(5)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(4)

    ShadowSystemClock.setElapsedRealtime(10)
    collector.register(listenerB, sensorB)

    ShadowSystemClock.setElapsedRealtime(13)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(12 + 3)

    ShadowSystemClock.setElapsedRealtime(15)
    collector.unregister(listener, null)

    ShadowSystemClock.setElapsedRealtime(18)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(14 + 8)

    ShadowSystemClock.setElapsedRealtime(20)
    collector.unregister(listenerB, null)

    ShadowSystemClock.setElapsedRealtime(50)
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(14 + 10)
  }

  /**
   * ```
   * Time                = 01 .. 10 .. 15 .. 20 .. 50
   * Sensor A/Listener A = [............]
   * Sensor B/Listener B =       [............]
   * ```
   */
  @Test
  fun test_attribution_snapshot() {
    val sensorB: Sensor = mock()
    val listenerB: SensorEventListener = mock()

    whenever(sensorB.type).thenReturn(2)
    whenever(sensorB.power).thenReturn(100.0f)

    ShadowSystemClock.setElapsedRealtime(1)
    collector.register(listener, sensor)

    ShadowSystemClock.setElapsedRealtime(10)
    collector.register(listenerB, sensorB)

    ShadowSystemClock.setElapsedRealtime(15)
    collector.unregister(listener, null)

    ShadowSystemClock.setElapsedRealtime(20)
    collector.unregister(listenerB, null)

    ShadowSystemClock.setElapsedRealtime(50)

    metrics.isAttributionEnabled = true
    assertThat(collector.getSnapshot(metrics)).isTrue()
    assertThat(metrics.total.activeTimeMs).isEqualTo(14 + 10)
    assertThat(metrics.sensorConsumption.size()).isEqualTo(2)
    assertThat(metrics.sensorConsumption.get(1337).activeTimeMs).isEqualTo(14)
    assertThat(metrics.sensorConsumption.get(1337).powerMah)
        .isEqualTo(14.0 * sensor.power.toDouble() / 3600 / 1000)
    assertThat(metrics.sensorConsumption.get(2).activeTimeMs).isEqualTo(10)
    assertThat(metrics.sensorConsumption.get(2).powerMah)
        .isEqualTo(10.0 * sensorB.power.toDouble() / 3600 / 1000)
  }

  override fun getClazz(): Class<SensorMetricsCollector> = SensorMetricsCollector::class.java
}
