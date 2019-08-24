/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.core;

import androidx.annotation.Nullable;

/**
 * A utility class that composes collectors to reduce boilerplate for collectors that are used to
 * collect consecutive values: the most common use case for Battery Metrics.
 *
 * <p>Using this class makes it simple to share a single underlying collector instance, and manage
 * state externally.
 *
 * <pre>
 * {@code SystemMetricsCollector collector = ...; StatefulSystemMetricsCollector collector = new
 * StatefulSystemMetricsCollector(collector);
 *
 * // Every call gets the difference from the last call SystemMetrics metrics =
 * collector.getLatestDiffAndReset(); }</pre>
 *
 * <p>Note - creating a Stateful collector immediately takes an initial snapshot. - this class is
 * _not_ thread safe.
 */
public class StatefulSystemMetricsCollector<
    R extends SystemMetrics<R>, S extends SystemMetricsCollector<R>> {

  private final S mCollector;
  private final R mDiff;

  private R mCurr;
  private R mPrev;

  private boolean mIsValid = true;

  /**
   * Wrap the underlying collector to maintain external state: automatically takes a snapshot to
   * override the initial values.
   */
  public StatefulSystemMetricsCollector(S collector) {
    this(
        collector, collector.createMetrics(), collector.createMetrics(), collector.createMetrics());
    mIsValid &= collector.getSnapshot(mPrev);
  }

  /**
   * Wraps the underlying collector, but with custom metrics objects: useful for passing in custom
   * metrics objects, such as {@link com.facebook.battery.metrics.wakelock.WakeLockMetrics}.
   *
   * <p>Note that this doesn't auto-initialize the previous diff and mainly exists to make it
   * convenient to set a custom initial snapshot.
   */
  public StatefulSystemMetricsCollector(S collector, R curr, R prev, R diff) {
    mCollector = collector;
    mCurr = curr;
    mPrev = prev;
    mDiff = diff;
  }

  /** Access the underlying collector. */
  public S getCollector() {
    return mCollector;
  }

  /** Get a diff from the previous baseline and update it. */
  @Nullable
  public R getLatestDiffAndReset() {
    if (getLatestDiff() == null) {
      return null;
    }

    R temp = mPrev;
    mPrev = mCurr;
    mCurr = temp;
    return mDiff;
  }

  /** Get a diff form the previous baseline. */
  @Nullable
  public R getLatestDiff() {
    mIsValid &= mCollector.getSnapshot(this.mCurr);
    if (!mIsValid) {
      return null;
    }

    mCurr.diff(mPrev, mDiff);
    return mDiff;
  }
}
