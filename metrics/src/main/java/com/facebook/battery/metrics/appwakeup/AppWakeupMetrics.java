/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.appwakeup;

import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.battery.metrics.core.Utilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class contains the metrics to measure the usage of different background scheduling
 * mechanisms like Alarms, Jobschedulers, GCMNetworkManager. This is not an exhaustive list and we
 * can add more reasons in the future. We record the number of times and the total duration of
 * execution for each of these wakeups.
 */
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

    return Utilities.simpleArrayMapEquals(this.appWakeups, that.appWakeups);
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

  public JSONArray toJSON() throws JSONException {
    JSONArray jsonArray = new JSONArray();
    for (int i = 0; i < appWakeups.size(); i++) {
      JSONObject obj = new JSONObject();
      AppWakeupMetrics.WakeupDetails details = appWakeups.valueAt(i);
      obj.put("key", appWakeups.keyAt(i));
      obj.put("type", details.reason.toString());
      obj.put("count", details.count);
      obj.put("time_ms", details.wakeupTimeMs);
      jsonArray.put(obj);
    }
    return jsonArray;
  }

  /**
   * A utility class to store details related to a single wakeup - total count and total time of
   * execution in ms for the wakeup.
   */
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

    public WakeupDetails(WakeupReason reason, long count, long wakeupTimeMs) {
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
