// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.api;

public abstract class SystemMetricsCollector<T extends SystemMetrics> {

  /**
   * Note that access to snapshot instances is <em>not</em> synchronized and must be taken care of
   * by the caller requesting getSnapshot.
   *
   * @param snapshot snapshot on which the data will be written
   * @return true if the snapshot has been updated with valid data.
   */
  abstract public boolean getSnapshot(T snapshot);

  /**
   * Creates an empty instance of the corresponding system metrics.
   *
   * @return New System Metrics object
   */
  abstract public T createMetrics();
}
