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
import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.battery.metrics.core.Utilities;

/**
 * Maintains state about total active wakelocks and the current time available.
 *
 * <p>A wakelock metrics object can also maintain attribution data if enabled at creation time --
 * this will come at the cost of extra memory to maintain this information.
 */
public class WakeLockMetrics extends SystemMetrics<WakeLockMetrics> {

  /** Whether this object should also record attribution */
  public boolean isAttributionEnabled;

  /** Attribution data */
  public final SimpleArrayMap<String, Long> tagTimeMs = new SimpleArrayMap<>();

  /** Total held time */
  public long heldTimeMs;

  /** How many times wakelocks were acquired */
  public long acquiredCount;

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
      output.acquiredCount = acquiredCount + b.acquiredCount;
      if (output.isAttributionEnabled) {
        output.tagTimeMs.clear();
        for (int i = 0, size = tagTimeMs.size(); i < size; i++) {
          String tag = tagTimeMs.keyAt(i);
          Long currentTimeMs = b.tagTimeMs.get(tag);
          output.tagTimeMs.put(
              tag, tagTimeMs.valueAt(i) + (currentTimeMs == null ? 0 : currentTimeMs));
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
  public WakeLockMetrics diff(@Nullable WakeLockMetrics b, @Nullable WakeLockMetrics output) {
    if (output == null) {
      output = new WakeLockMetrics(isAttributionEnabled);
    }

    if (b == null) {
      output.set(this);
    } else {
      output.heldTimeMs = heldTimeMs - b.heldTimeMs;
      output.acquiredCount = acquiredCount - b.acquiredCount;
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
    acquiredCount = b.acquiredCount;
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

    if (isAttributionEnabled != that.isAttributionEnabled
        || heldTimeMs != that.heldTimeMs
        || acquiredCount != that.acquiredCount) {
      return false;
    }

    if (!Utilities.simpleArrayMapEquals(tagTimeMs, that.tagTimeMs)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = (isAttributionEnabled ? 1 : 0);
    result = 31 * result + tagTimeMs.hashCode();
    result = 31 * result + (int) (heldTimeMs ^ (heldTimeMs >>> 32));
    result = 31 * result + (int) (acquiredCount ^ (acquiredCount >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "WakeLockMetrics{"
        + "isAttributionEnabled="
        + isAttributionEnabled
        + ", tagTimeMs="
        + tagTimeMs
        + ", heldTimeMs="
        + heldTimeMs
        + ", acquiredCount="
        + acquiredCount
        + '}';
  }
}
