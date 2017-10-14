/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.bluetooth;

import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.infer.annotation.ThreadSafe;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A box of values for recording all bluetooth scans; requires custom instrumentation. Trigger with
 * {@link #addScan(long)} to record a bluetooth scan.
 */
@ThreadSafe
public class BluetoothMetricsCollector extends SystemMetricsCollector<BluetoothMetrics> {

  private int bleScanCount;
  private long bleScanDurationMs;
  private final ReentrantReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(BluetoothMetrics snapshot) {
    if (snapshot == null) {
      throw new IllegalArgumentException("Null value passed to getSnapshot!");
    }
    mReadWriteLock.readLock().lock();
    try {
      snapshot.bleScanCount = bleScanCount;
      snapshot.bleScanDurationMs = bleScanDurationMs;
      return true;
    } finally {
      mReadWriteLock.readLock().unlock();
    }
  }

  @Override
  public BluetoothMetrics createMetrics() {
    return new BluetoothMetrics();
  }

  /** Record a bluetooth scan with the duration it ran for. */
  public void addScan(long durationMs) {
    if (durationMs > 0) {
      mReadWriteLock.writeLock().lock();
      try {
        bleScanCount++;
        bleScanDurationMs += durationMs;
      } finally {
        mReadWriteLock.writeLock().unlock();
      }
    }
  }
}
