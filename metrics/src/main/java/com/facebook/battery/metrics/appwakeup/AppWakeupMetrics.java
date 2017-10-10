/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.appwakeup;

import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import com.facebook.battery.metrics.api.SystemMetrics;
import com.facebook.battery.metrics.api.SystemMetricsLogger;

/** Information about app wakeup reasons */
public class AppWakeupMetrics extends SystemMetrics<AppWakeupMetrics> {

  public enum WakeupReason {
    JOB_SCHEDULER,
    GCM,
    ALARM
  }

  public SimpleArrayMap<String, WakeupDetails> appWakeups = new SimpleArrayMap<>();

  @Override
  public AppWakeupMetrics set(AppWakeupMetrics metrics) {
    appWakeups.clear();
    appWakeups.putAll(metrics.appWakeups);
    return this;
  }

  @Override
  public AppWakeupMetrics sum(@Nullable AppWakeupMetrics b, @Nullable AppWakeupMetrics output) {
    if (output == null) {
      output = new AppWakeupMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      // TODO:
      // We can avoid clear and reallocation by intersecting the output.appWakeups and
      // this.appWakeups. Not sure if the code complexity and computation cost is worth the
      // savings. Hence, will do it if needed in the future.
      output.appWakeups.clear();
      for (int i = 0; i < appWakeups.size(); i++) {
        String tag = appWakeups.keyAt(i);
        output.appWakeups.put(tag, new WakeupDetails(appWakeups.valueAt(i).reason));
        appWakeups.valueAt(i).sum(b.appWakeups.get(tag), output.appWakeups.get(tag));
      }
      for (int i = 0; i < b.appWakeups.size(); i++) {
        String tag = b.appWakeups.keyAt(i);
        if (!output.appWakeups.containsKey(tag)) {
          output.appWakeups.put(tag, b.appWakeups.valueAt(i));
        }
      }
    }
    return output;
  }

  @Override
  public AppWakeupMetrics diff(@Nullable AppWakeupMetrics b, @Nullable AppWakeupMetrics output) {
    if (output == null) {
      output = new AppWakeupMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.appWakeups.clear();
      for (int i = 0; i < appWakeups.size(); i++) {
        String tag = appWakeups.keyAt(i);
        output.appWakeups.put(tag, new WakeupDetails(appWakeups.valueAt(i).reason));
        appWakeups.valueAt(i).diff(b.appWakeups.get(tag), output.appWakeups.get(tag));
      }
    }
    return output;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AppWakeupMetrics that = (AppWakeupMetrics) o;

    // SimpleArrayMap has a broken equals implementation up till
    // https://github.com/android/platform_frameworks_support/commit/5b6d31ca0497e11d9af12810fefbc81a88f75d22?diff=split
    // Explicitly extracted the relevant comparison code accordingly.
    if (this.appWakeups.size() != that.appWakeups.size()) {
      return false;
    }
    for (int i = 0; i < appWakeups.size(); i++) {
      String key = appWakeups.keyAt(i);
      WakeupDetails mine = appWakeups.valueAt(i);
      WakeupDetails theirs = that.appWakeups.get(key);
      if (mine == null) {
        if (theirs != null || !that.appWakeups.containsKey(key)) {
          return false;
        }
      } else if (!mine.equals(theirs)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return appWakeups != null ? appWakeups.hashCode() : 0;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < appWakeups.size(); i++) {
      sb.append(appWakeups.keyAt(i)).append(": ").append(appWakeups.valueAt(i)).append(", ");
    }
    return sb.toString();
  }

  public static class WakeupDetails {
    public WakeupReason reason;
    public long count;
    public long wakeupTimeMs;

    public WakeupDetails() {
      count = 0;
      wakeupTimeMs = 0;
    }

    public WakeupDetails(WakeupReason reason) {
      this(reason, 0, 0);
    }

    public WakeupDetails(WakeupReason reason, int count, long wakeupTimeMs) {
      this.reason = reason;
      this.count = count;
      this.wakeupTimeMs = wakeupTimeMs;
    }

    public WakeupDetails set(WakeupDetails details) {
      this.reason = details.reason;
      this.count = details.count;
      this.wakeupTimeMs = details.wakeupTimeMs;
      return this;
    }

    public WakeupDetails sum(@Nullable WakeupDetails b, WakeupDetails output) {
      if (b == null) {
        return output.set(this);
      }
      if (b.reason != reason) {
        SystemMetricsLogger.wtf(
            "AppWakeupMetrics",
            "Sum only allowed for similar wakeups: " + this.toString() + ", " + b.toString());
      }
      output.reason = reason;
      output.count = this.count + b.count;
      output.wakeupTimeMs = this.wakeupTimeMs + b.wakeupTimeMs;
      return this;
    }

    public WakeupDetails diff(@Nullable WakeupDetails b, WakeupDetails output) {
      if (b == null) {
        return output.set(this);
      }
      if (b.reason != reason) {
        SystemMetricsLogger.wtf(
            "AppWakeupMetrics",
            "Diff only allowed for similar kind of wakeups: "
                + this.toString()
                + ", "
                + b.toString());
      }
      output.reason = reason;
      output.count = this.count - b.count;
      output.wakeupTimeMs = this.wakeupTimeMs - b.wakeupTimeMs;
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      WakeupDetails that = (WakeupDetails) o;

      if (count != that.count) return false;
      if (wakeupTimeMs != that.wakeupTimeMs) return false;
      return reason == that.reason;
    }

    @Override
    public int hashCode() {
      int result = reason != null ? reason.hashCode() : 0;
      result = 31 * result + (int) (count ^ (count >>> 32));
      result = 31 * result + (int) (wakeupTimeMs ^ (wakeupTimeMs >>> 32));
      return result;
    }

    public String toString() {
      return "{reason=" + reason + ", count=" + count + ", wakeupTimeMs=" + wakeupTimeMs + "}";
    }
  }
}
