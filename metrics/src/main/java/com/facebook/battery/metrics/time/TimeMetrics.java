/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.time;

import android.support.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;

/**
 * Maintain uptime and realtime for the application: remember to use real time for normalizing
 * metrics for comparison.
 */
public class TimeMetrics extends SystemMetrics<TimeMetrics> {

  private static final long serialVersionUID = 0;

  public long uptimeMs;
  public long realtimeMs;

  public TimeMetrics() {}

  @Override
  public TimeMetrics set(TimeMetrics metrics) {
    uptimeMs = metrics.uptimeMs;
    realtimeMs = metrics.realtimeMs;
    return this;
  }

  public TimeMetrics sum(@Nullable TimeMetrics b, @Nullable TimeMetrics output) {
    if (output == null) {
      output = new TimeMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.uptimeMs = uptimeMs + b.uptimeMs;
      output.realtimeMs = realtimeMs + b.realtimeMs;
    }

    return output;
  }

  @Override
  public TimeMetrics diff(@Nullable TimeMetrics b, @Nullable TimeMetrics output) {
    if (output == null) {
      output = new TimeMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.uptimeMs = uptimeMs - b.uptimeMs;
      output.realtimeMs = realtimeMs - b.realtimeMs;
    }

    return output;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TimeMetrics that = (TimeMetrics) o;

    return uptimeMs == that.uptimeMs && realtimeMs == that.realtimeMs;
  }

  @Override
  public int hashCode() {
    int result = (int) (uptimeMs ^ (uptimeMs >>> 32));
    result = 31 * result + (int) (realtimeMs ^ (realtimeMs >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "TimeMetrics{" + "uptimeMs=" + uptimeMs + ", realtimeMs=" + realtimeMs + '}';
  }
}
