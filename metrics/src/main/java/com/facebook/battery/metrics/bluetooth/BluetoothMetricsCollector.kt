/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.bluetooth

import android.app.PendingIntent
import android.bluetooth.le.ScanCallback
import android.os.SystemClock
import android.util.SparseArray
import androidx.annotation.GuardedBy
import com.facebook.battery.metrics.core.SystemMetricsCollector
import com.facebook.battery.metrics.core.Utilities
import com.facebook.infer.annotation.ThreadSafe

/**
 * Records information about Bluetooth LE scans, meant to be used with [BluetoothLeScanner].
 *
 * The opportunistic scan mode where the application will passively listen for results from other
 * scans without starting the scan itself, is a special scan mode and will be metered separately.
 *
 * Just like the implementation in BluetoothLeScanner and GattService, scanners are identified by
 * the [ScanCallback] or [PendingIntent] object being passed to [BluetoothLeScanner#startScan].
 */
@ThreadSafe
class BluetoothMetricsCollector : SystemMetricsCollector<BluetoothMetrics?>() {

  private class ScanMetrics {
    var startTime: Long = 0
    var duration: Long = 0
    var count: Int = 0
    var numActive: Int = 0

    val totalDuration: Long
      get() = duration + (if (numActive > 0) (SystemClock.uptimeMillis() - startTime) else 0)
  }

  /** Keep track of active scans and whether they are opportunistic or not. */
  @GuardedBy("this") private val isOpportunistic = SparseArray<Boolean?>()

  @GuardedBy("this") private val nonOpportunisticScan = ScanMetrics()

  @GuardedBy("this") private val opportunisticScan = ScanMetrics()

  @Synchronized
  override fun getSnapshot(snapshot: BluetoothMetrics): Boolean {
    Utilities.checkNotNull(snapshot, "Null value passed to getSnapshot!")
    snapshot.bleScanCount = nonOpportunisticScan.count
    snapshot.bleOpportunisticScanCount = opportunisticScan.count
    snapshot.bleScanDurationMs = nonOpportunisticScan.totalDuration
    snapshot.bleOpportunisticScanDurationMs = opportunisticScan.totalDuration
    return true
  }

  override fun createMetrics(): BluetoothMetrics = BluetoothMetrics()

  fun startScan(callback: ScanCallback, isOpportunistic: Boolean) {
    startScanImpl(callback.hashCode(), isOpportunistic)
  }

  fun startScan(callback: PendingIntent, isOpportunistic: Boolean) {
    startScanImpl(callback.hashCode(), isOpportunistic)
  }

  fun stopScan(callback: ScanCallback) {
    stopScanImpl(callback.hashCode())
  }

  fun stopScan(callback: PendingIntent) {
    stopScanImpl(callback.hashCode())
  }

  private fun getScanMetrics(isOpportunistic: Boolean): ScanMetrics =
      if (isOpportunistic) opportunisticScan else nonOpportunisticScan

  @Synchronized
  private fun startScanImpl(scannerId: Int, isOpportunistic: Boolean) {
    if (this.isOpportunistic[scannerId] != null) {
      // ignore if scan already started
      return
    }
    this.isOpportunistic.put(scannerId, isOpportunistic)

    val scanMetrics = this.getScanMetrics(isOpportunistic)
    if (scanMetrics.numActive == 0) {
      scanMetrics.startTime = SystemClock.uptimeMillis()
    }
    scanMetrics.count++
    scanMetrics.numActive++
  }

  @Synchronized
  private fun stopScanImpl(scannerId: Int) {
    val isOpportunistic =
        isOpportunistic[scannerId]
            ?: // ignore if scan is inactive
            return
    this.isOpportunistic.remove(scannerId)

    val scanMetrics = this.getScanMetrics(isOpportunistic)
    scanMetrics.numActive--
    if (scanMetrics.numActive == 0) {
      scanMetrics.duration += SystemClock.uptimeMillis() - scanMetrics.startTime
    }
  }
}
