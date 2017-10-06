// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.api;

import android.support.annotation.Nullable;

/**
 * A utility class that composes collectors to reduce boilerplate for collectors that are used
 * to collect consecutive values.
 */
public class StatefulSystemMetricsCollector<
    R extends SystemMetrics<R>,
    S extends SystemMetricsCollector<R>> {

  private final S mCollector;
  private final R mDiff;

  private R mCurr;
  private R mPrev;

  private boolean mIsValid = true;

  public StatefulSystemMetricsCollector(S collector) {
    this(
        collector,
        collector.createMetrics(),
        collector.createMetrics(),
        collector.createMetrics());
  }

  public StatefulSystemMetricsCollector(
      S collector,
      R curr,
      R prev,
      R diff) {
    mCollector = collector;
    mCurr = curr;
    mPrev = prev;
    mDiff = diff;

    mIsValid &= mCollector.getSnapshot(this.mPrev);
  }

  public S getCollector() {
    return mCollector;
  }

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
