// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.time;

import android.support.annotation.Nullable;
import com.facebook.battery.metrics.api.SystemMetrics;

public class TimeMetrics extends SystemMetrics<TimeMetrics> {

  private static final long serialVersionUID = 0;

  public long uptimeMs;
  public long realtimeMs;

  public TimeMetrics() {
  }

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

    return uptimeMs == that.uptimeMs &&
      realtimeMs == that.realtimeMs;
  }

  @Override
  public int hashCode() {
    int result = (int) (uptimeMs ^ (uptimeMs >>> 32));
    result = 31 * result + (int) (realtimeMs ^ (realtimeMs >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "TimeMetrics{" +
        "uptimeMs=" + uptimeMs +
        ", realtimeMs=" + realtimeMs +
        '}';
  }
}
