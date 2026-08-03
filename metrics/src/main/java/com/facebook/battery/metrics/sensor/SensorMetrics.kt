/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.sensor

import android.util.SparseArray
import com.facebook.battery.metrics.core.SystemMetrics
import com.facebook.battery.metrics.core.Utilities
import org.json.JSONException
import org.json.JSONObject

class SensorMetrics @JvmOverloads constructor(@JvmField var isAttributionEnabled: Boolean = false) :
    SystemMetrics<SensorMetrics>() {

  @JvmField val total: Consumption = Consumption()

  @JvmField // SparseArray is Android-specific and not Serializable
  // SparseArray is Android-specific and not Serializable
  val sensorConsumption: SparseArray<Consumption> = SparseArray()

  override fun sum(b: SensorMetrics?, output: SensorMetrics?): SensorMetrics {
    var output = output
    if (output == null) {
      output = SensorMetrics(isAttributionEnabled)
    }

    if (b == null) {
      output.set(this)
    } else {
      total.sum(b.total, output.total)

      if (output.isAttributionEnabled) {
        op(+1, sensorConsumption, b.sensorConsumption, output.sensorConsumption)
      }
    }

    return output
  }

  override fun diff(b: SensorMetrics?, output: SensorMetrics?): SensorMetrics {
    var output = output
    if (output == null) {
      output = SensorMetrics(isAttributionEnabled)
    }

    if (b == null) {
      output.set(this)
    } else {
      total.diff(b.total, output.total)
      if (output.isAttributionEnabled) {
        op(-1, sensorConsumption, b.sensorConsumption, output.sensorConsumption)
      }
    }

    return output
  }

  override fun set(b: SensorMetrics): SensorMetrics {
    total.set(b.total)

    if (isAttributionEnabled && b.isAttributionEnabled) {
      sensorConsumption.clear()
      var i = 0
      val l = b.sensorConsumption.size()
      while (i < l) {
        sensorConsumption.put(b.sensorConsumption.keyAt(i), b.sensorConsumption.valueAt(i))
        i++
      }
    }

    return this
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    if (other == null || javaClass != other.javaClass) {
      return false
    }

    val that = other as SensorMetrics
    return isAttributionEnabled == that.isAttributionEnabled &&
        total == that.total &&
        Utilities.sparseArrayEquals(sensorConsumption, that.sensorConsumption)
  }

  override fun hashCode(): Int {
    var result = if (this.isAttributionEnabled) 1 else 0
    result = 31 * result + total.hashCode()
    result = 31 * result + sensorConsumption.hashCode()
    return result
  }

  private fun op(
      sign: Int,
      a: SparseArray<Consumption>,
      b: SparseArray<Consumption>,
      output: SparseArray<Consumption>,
  ) {
    output.clear()

    run {
      var i = 0
      val l = a.size()
      while (i < l) {
        val key = a.keyAt(i)
        val result =
            if (sign > 0) {
              a.valueAt(i)!!.sum(b[key, ZERO])
            } else {
              a.valueAt(i)!!.diff(b[key, ZERO])
            }

        if (ZERO != result) {
          output.put(key, result)
        }
        i++
      }
    }

    var j = 0
    val l = b.size()
    while (j < l) {
      val key = b.keyAt(j)
      if (a[key] == null) {
        val result =
            if (sign > 0) {
              ZERO.sum(b.valueAt(j))
            } else {
              ZERO.diff(b.valueAt(j))
            }

        if (ZERO != result) {
          output.put(key, result)
        }
      }
      j++
    }
  }

  override fun toString(): String =
      "SensorMetrics{isAttributionEnabled=${isAttributionEnabled}, total=${total}, sensorConsumption=${sensorConsumption}${'}'}"

  @Throws(JSONException::class)
  fun attributionToJSONObject(): JSONObject? {
    if (!isAttributionEnabled) {
      return null
    }

    val attribution = JSONObject()
    var i = 0
    val l = sensorConsumption.size()
    while (i < l) {
      val value = sensorConsumption.valueAt(i)
      val details = JSONObject()
      details.put("activeTimeMs", value!!.activeTimeMs)
      details.put("wakeUpTimeMs", value.wakeUpTimeMs)
      details.put("powerMah", value.powerMah)
      attribution.put(sensorConsumption.keyAt(i).toString(), details)
      i++
    }
    return attribution
  }

  class Consumption : SystemMetrics<Consumption> {
    @JvmField var powerMah: Double = 0.0

    @JvmField var activeTimeMs: Long = 0

    @JvmField var wakeUpTimeMs: Long = 0

    constructor()

    constructor(powerMah: Double, activeTimeMs: Long, wakeUpTimeMs: Long) {
      this.powerMah = powerMah
      this.activeTimeMs = activeTimeMs
      this.wakeUpTimeMs = wakeUpTimeMs
    }

    override fun sum(b: Consumption?, output: Consumption?): Consumption {
      var output = output
      if (output == null) {
        output = SensorMetrics.Consumption()
      }

      if (b == null) {
        output.set(this)
      } else {
        output.powerMah = b.powerMah + powerMah
        output.activeTimeMs = b.activeTimeMs + activeTimeMs
        output.wakeUpTimeMs = b.wakeUpTimeMs + wakeUpTimeMs
      }

      return output
    }

    override fun diff(b: Consumption?, output: Consumption?): Consumption {
      var output = output
      if (output == null) {
        output = SensorMetrics.Consumption()
      }

      if (b == null) {
        output.set(this)
      } else {
        output.powerMah = powerMah - b.powerMah
        output.activeTimeMs = activeTimeMs - b.activeTimeMs
        output.wakeUpTimeMs = wakeUpTimeMs - b.wakeUpTimeMs
      }

      return output
    }

    override fun set(b: Consumption): Consumption {
      this.powerMah = b.powerMah
      this.activeTimeMs = b.activeTimeMs
      this.wakeUpTimeMs = b.wakeUpTimeMs
      return this
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) {
        return true
      }
      if (other == null || javaClass != other.javaClass) {
        return false
      }

      val that = other as Consumption
      return java.lang.Double.compare(
          that.powerMah,
          powerMah,
      ) == 0 && activeTimeMs == that.activeTimeMs && wakeUpTimeMs == that.wakeUpTimeMs
    }

    override fun hashCode(): Int {
      var result: Int
      val temp = java.lang.Double.doubleToLongBits(powerMah)
      result = (temp xor (temp ushr 32)).toInt()
      result = 31 * result + (activeTimeMs xor (activeTimeMs ushr 32)).toInt()
      result = 31 * result + (wakeUpTimeMs xor (wakeUpTimeMs ushr 32)).toInt()
      return result
    }

    override fun toString(): String =
        "Consumption{powerMah=${powerMah}, activeTimeMs=${activeTimeMs}, wakeUpTimeMs=${wakeUpTimeMs}${'}'}"

    companion object {
      private const val serialVersionUID = 1L
    }
  }

  companion object {
    private const val serialVersionUID = 1L

    private val ZERO = SensorMetrics.Consumption()
  }
}
