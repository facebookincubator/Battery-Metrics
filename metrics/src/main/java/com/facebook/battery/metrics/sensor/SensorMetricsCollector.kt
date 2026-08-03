/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.sensor

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.os.SystemClock
import androidx.annotation.GuardedBy
import androidx.collection.SimpleArrayMap
import com.facebook.battery.metrics.core.SystemMetricsCollector
import com.facebook.battery.metrics.core.Utilities
import com.facebook.infer.annotation.ThreadSafe
import kotlin.concurrent.Volatile

@ThreadSafe
class SensorMetricsCollector : SystemMetricsCollector<SensorMetrics>() {

  private class SensorListenerData(val listener: SensorEventListener, val sensor: Sensor)

  private class SensorData(var startTimeMs: Long, var activeCount: Int)

  @GuardedBy("this") @Volatile private var enabled = true

  override fun hashCode(): Int = super.hashCode()

  @GuardedBy("this") private val activeSensorData = ArrayList<SensorListenerData>()

  @GuardedBy("this") private val activeSensors = SimpleArrayMap<Sensor, SensorData?>()

  @GuardedBy("this") private val cumulativeMetrics = SensorMetrics(true)

  @Synchronized
  fun disable() {
    enabled = false
    activeSensors.clear()
  }

  @Synchronized
  fun register(listener: SensorEventListener, sensor: Sensor) {
    if (!enabled) {
      return
    }

    val data = SensorListenerData(listener, sensor)
    activeSensorData.add(data)
    var currentCount = activeSensors[sensor]
    if (currentCount == null) {
      currentCount = SensorData(SystemClock.elapsedRealtime(), 1)
      activeSensors.put(sensor, currentCount)
    } else {
      currentCount.activeCount++
    }
  }

  @Synchronized
  fun unregister(listener: SensorEventListener, sensor: Sensor?) {
    if (!enabled) {
      return
    }

    val currentTimeMs = SystemClock.elapsedRealtime()
    val iter = activeSensorData.iterator()
    while (iter.hasNext()) {
      val data = iter.next()

      if (listener !== data.listener || (sensor != null && sensor != data.sensor)) {
        continue
      }

      iter.remove()

      val currentSensor = activeSensors[data.sensor]
      if (currentSensor == null || currentSensor.activeCount == 0) {
        // Spurious / extra call
        continue
      } else if (currentSensor.activeCount > 1) {
        // No additional book-keeping required at the moment
        currentSensor.activeCount -= 1
        continue
      }

      // Adjust for sensor's consumption
      activeSensors.remove(data.sensor)

      val type = data.sensor.type

      var consumption = cumulativeMetrics.sensorConsumption[type, null]
      if (consumption == null) {
        consumption = SensorMetrics.Consumption()
        cumulativeMetrics.sensorConsumption.put(type, consumption)
      }

      val currentActiveTimeMs = currentTimeMs - currentSensor.startTimeMs
      consumption.activeTimeMs += currentActiveTimeMs
      cumulativeMetrics.total.activeTimeMs += currentActiveTimeMs

      val currentPowerMah = energyConsumedMah(data.sensor, currentActiveTimeMs)
      consumption.powerMah += currentPowerMah
      cumulativeMetrics.total.powerMah += currentPowerMah

      if (Util.isWakeupSensor(data.sensor)) {
        consumption.wakeUpTimeMs += currentActiveTimeMs
        cumulativeMetrics.total.wakeUpTimeMs += currentActiveTimeMs
      }
    }
  }

  @Synchronized
  override fun getSnapshot(snapshot: SensorMetrics): Boolean {
    Utilities.checkNotNull(snapshot, "Null value passed to getSnapshot!")

    if (!enabled) {
      return false
    }

    val currentTimeMs = SystemClock.elapsedRealtime()
    snapshot.set(cumulativeMetrics)

    var i = 0
    val l = activeSensors.size()
    while (i < l) {
      val sensor = activeSensors.keyAt(i)
      val data = activeSensors.valueAt(i)

      if (data == null) {
        i++
        continue
      }

      if (data.activeCount <= 0) {
        i++
        continue
      }

      val sensorActiveTimeMs = currentTimeMs - data.startTimeMs
      val sensorPowerMah = energyConsumedMah(sensor, sensorActiveTimeMs)
      snapshot.total.activeTimeMs += sensorActiveTimeMs
      snapshot.total.powerMah += sensorPowerMah

      val isWakeupSensor = Util.isWakeupSensor(sensor)
      if (isWakeupSensor) {
        snapshot.total.wakeUpTimeMs += sensorActiveTimeMs
      }

      if (snapshot.isAttributionEnabled) {
        val type = sensor.type
        var consumption = snapshot.sensorConsumption[type]
        if (consumption == null) {
          consumption = SensorMetrics.Consumption()
          snapshot.sensorConsumption.put(type, consumption)
        }

        consumption.activeTimeMs += sensorActiveTimeMs
        consumption.powerMah += sensorPowerMah

        if (isWakeupSensor) {
          consumption.wakeUpTimeMs += sensorActiveTimeMs
        }
      }
      i++
    }

    return true
  }

  override fun createMetrics(): SensorMetrics = SensorMetrics()

  private object Util {
    @JvmStatic fun isWakeupSensor(sensor: Sensor): Boolean = sensor.isWakeUpSensor
  }

  companion object {
    private fun energyConsumedMah(sensor: Sensor, activeTimeMs: Long): Double {
      val sensorPowerMa = sensor.power.toDouble()
      return sensorPowerMa * activeTimeMs / 3_600 / 1_000
    }
  }
}
