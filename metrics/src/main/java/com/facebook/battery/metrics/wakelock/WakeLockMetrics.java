/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.wakelock;

import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import com.facebook.battery.metrics.api.SystemMetrics;

/**
 * Maintains state about total active wakelocks and the current time available.
 *
 * <p>A wakelock metrics object can also maintain attribution data if enabled at creation time --
 * this will come at the cost of extra memory to maintain this information.
 */
public class WakeLockMetrics extends SystemMetrics<WakeLockMetrics> {

  public final SimpleArrayMap<String, Long> tagTimeMs = new SimpleArrayMap<>();
  public boolean isAttributionEnabled;
  public long heldTimeMs;

  /** Create a WakeLockMetrics object without attribution enabled. */
  public WakeLockMetrics() {
    this(false);
  }

  /** Create a WakeLockMetrics object and enable attribution based on the argument. */
  public WakeLockMetrics(boolean isAttributionEnabled) {
    this.isAttributionEnabled = isAttributionEnabled;
  }

  @Override
  public WakeLockMetrics sum(@Nullable WakeLockMetrics b, @Nullable WakeLockMetrics output) {
    if (output == null) {
      output = new WakeLockMetrics(isAttributionEnabled);
    }

    if (b == null) {
      output.set(this);
    } else {
      output.heldTimeMs = heldTimeMs + b.heldTimeMs;
      if (output.isAttributionEnabled) {
        output.tagTimeMs.clear();
        for (int i = 0, size = tagTimeMs.size(); i < size; i++) {
          String tag = tagTimeMs.keyAt(i);
          Long currentTimeMs = b.tagTimeMs.get(tag);
          output.tagTimeMs.put(
              tag,
              tagTimeMs.valueAt(i) + (currentTimeMs == null ? 0 : currentTimeMs));
        }
        for (int i = 0, size = b.tagTimeMs.size(); i < size; i++) {
          String tag = b.tagTimeMs.keyAt(i);
          if (tagTimeMs.get(tag) == null) {
            output.tagTimeMs.put(tag, b.tagTimeMs.valueAt(i));
          }
        }
      }
    }

    return output;
  }

  @Override
  public WakeLockMetrics diff(
      @Nullable WakeLockMetrics b, @Nullable WakeLockMetrics output) {
    if (output == null) {
      output = new WakeLockMetrics(isAttributionEnabled);
    }

    if (b == null) {
      output.set(this);
    } else {
      output.heldTimeMs = heldTimeMs - b.heldTimeMs;
      if (output.isAttributionEnabled) {
        output.tagTimeMs.clear();
        for (int i = 0, size = tagTimeMs.size(); i < size; i++) {
          String tag = tagTimeMs.keyAt(i);
          Long currentTimeMs = b.tagTimeMs.get(tag);
          long difference = tagTimeMs.valueAt(i) - (currentTimeMs == null ? 0 : currentTimeMs);
          if (difference != 0) {
            output.tagTimeMs.put(tag, difference);
          }
        }
      }
    }

    return output;
  }

  @Override
  public WakeLockMetrics set(WakeLockMetrics b) {
    heldTimeMs = b.heldTimeMs;
    if (b.isAttributionEnabled && isAttributionEnabled) {
      tagTimeMs.clear();
      tagTimeMs.putAll(b.tagTimeMs);
    }
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WakeLockMetrics that = (WakeLockMetrics) o;

    if (isAttributionEnabled != that.isAttributionEnabled ||
        heldTimeMs != that.heldTimeMs) {
      return false;
    }

    // SimpleArrayMap has a broken equals implementation up till
    // https://github.com/android/platform_frameworks_support/commit/5b6d31ca0497e11d9af12810fefbc81a88f75d22?diff=split
    // Explicitly extracted the relevant comparison code accordingly.
    if (tagTimeMs.size() != that.tagTimeMs.size()) {
      return false;
    }

    for (int i = 0, size = tagTimeMs.size(); i < size; i++) {
      String key = tagTimeMs.keyAt(i);
      Long mine = tagTimeMs.valueAt(i);
      Long theirs = that.tagTimeMs.get(key);
      if (mine == null) {
        if (theirs != null || !that.tagTimeMs.containsKey(key)) {
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
    int result = (isAttributionEnabled ? 1 : 0);
    result = 31 * result + tagTimeMs.hashCode();
    result = 31 * result + (int) (heldTimeMs ^ (heldTimeMs >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "WakeLockMetrics{" +
        "isAttributionEnabled=" + isAttributionEnabled +
        ", tagTimeMs=" + tagTimeMs +
        ", heldTimeMs=" + heldTimeMs +
        '}';
  }
}
