/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.bluetooth;

import static com.facebook.battery.metrics.core.Utilities.checkNotNull;

import android.app.PendingIntent;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.os.SystemClock;
import android.util.SparseArray;
import androidx.annotation.GuardedBy;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.infer.annotation.Nullsafe;
import com.facebook.infer.annotation.ThreadSafe;

/**
 * Records information about Bluetooth LE scans, meant to be used with {@link BluetoothLeScanner}.
 *
 * <p>The opportunistic scan mode where the application will passively listen for results from other
 * scans without starting the scan itself, is a special scan mode and will be metered separately.
 *
 * <p>Just like the implementation in BluetoothLeScanner and GattService, scanners are identified by
 * the {@link ScanCallback} or {@link PendingIntent} object being passed to {@link
 * BluetoothLeScanner#startScan}.
 */
@Nullsafe(Nullsafe.Mode.LOCAL)
@ThreadSafe
public class BluetoothMetricsCollector extends SystemMetricsCollector<BluetoothMetrics> {

  private static class ScanMetrics {
    public long startTime;
    public long duration;
    public int count;
    public int numActive;

    public long getTotalDuration() {
      return duration + (numActive > 0 ? SystemClock.uptimeMillis() - startTime : 0);
    }
  }

  /** Keep track of active scans and whether they are opportunistic or not. */
  @GuardedBy("this")
  private final SparseArray<Boolean> mIsOpportunistic = new SparseArray<>();

  @GuardedBy("this")
  private final ScanMetrics mNonOpportunisticScan = new ScanMetrics();

  @GuardedBy("this")
  private final ScanMetrics mOpportunisticScan = new ScanMetrics();

  @Override
  public synchronized boolean getSnapshot(BluetoothMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");
    snapshot.bleScanCount = mNonOpportunisticScan.count;
    snapshot.bleOpportunisticScanCount = mOpportunisticScan.count;
    snapshot.bleScanDurationMs = mNonOpportunisticScan.getTotalDuration();
    snapshot.bleOpportunisticScanDurationMs = mOpportunisticScan.getTotalDuration();
    return true;
  }

  @Override
  public BluetoothMetrics createMetrics() {
    return new BluetoothMetrics();
  }

  public void startScan(ScanCallback callback, boolean isOpportunistic) {
    startScanImpl(callback.hashCode(), isOpportunistic);
  }

  public void startScan(PendingIntent callback, boolean isOpportunistic) {
    startScanImpl(callback.hashCode(), isOpportunistic);
  }

  public void stopScan(ScanCallback callback) {
    stopScanImpl(callback.hashCode());
  }

  public void stopScan(PendingIntent callback) {
    stopScanImpl(callback.hashCode());
  }

  private ScanMetrics getScanMetrics(boolean isOpportunistic) {
    return isOpportunistic ? mOpportunisticScan : mNonOpportunisticScan;
  }

  private synchronized void startScanImpl(int scannerId, boolean isOpportunistic) {
    if (mIsOpportunistic.get(scannerId) != null) {
      // ignore if scan already started
      return;
    }
    mIsOpportunistic.put(scannerId, isOpportunistic);

    ScanMetrics scanMetrics = getScanMetrics(isOpportunistic);
    if (scanMetrics.numActive == 0) {
      scanMetrics.startTime = SystemClock.uptimeMillis();
    }
    scanMetrics.count++;
    scanMetrics.numActive++;
  }

  private synchronized void stopScanImpl(int scannerId) {
    Boolean isOpportunistic = mIsOpportunistic.get(scannerId);
    if (isOpportunistic == null) {
      // ignore if scan is inactive
      return;
    }
    mIsOpportunistic.remove(scannerId);

    ScanMetrics scanMetrics = getScanMetrics(isOpportunistic);
    scanMetrics.numActive--;
    if (scanMetrics.numActive == 0) {
      scanMetrics.duration += SystemClock.uptimeMillis() - scanMetrics.startTime;
    }
  }
}
