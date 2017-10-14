/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.core;

/**
 * Takes snapshots of a given metric. There are generally two types of metrics collectors - - those
 * that depend on an underlying api, such as the {@link
 * com.facebook.battery.metrics.cpu.CpuMetricsCollector} which reads from /proc. - those that are
 * simply boxes of values and are triggered by custom instrumentation, such as the {@link
 * com.facebook.battery.metrics.camera.CameraMetricsCollector} which should be triggered with every
 * call to the android camera api.
 */
public abstract class SystemMetricsCollector<T extends SystemMetrics> {

  /**
   * Note that access to snapshot instances is <em>not</em> synchronized and must be taken care of
   * by the caller requesting getSnapshot.
   *
   * @param snapshot snapshot on which the data will be written
   * @return true if the snapshot has been updated with valid data.
   */
  public abstract boolean getSnapshot(T snapshot);

  /**
   * Creates an empty instance of the corresponding system metrics.
   *
   * <p>Explicitly having this function available reduces a significant amount of boilerplate
   *
   * @return New System Metrics object
   */
  public abstract T createMetrics();
}
