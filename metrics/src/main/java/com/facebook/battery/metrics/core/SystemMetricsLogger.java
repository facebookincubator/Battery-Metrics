/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.core;

import android.support.annotation.Nullable;
import android.util.Log;

/**
 * A utility class to collect logs from the battery metrics library: it's generally a good idea to
 * hook this up with error trace collection to sanity check against any errors in the Metrics
 * Collectors.
 */
public final class SystemMetricsLogger {

  public interface Delegate {
    void wtf(String Tag, String message, @Nullable Throwable cause);
  }

  private static Delegate sDelegate = null;

  /**
   * Set a custom logging implementation: if there is none, then the library will simply log to
   * Logcat.
   */
  public static void setDelegate(Delegate delegate) {
    sDelegate = delegate;
  }

  /** Log an unexpected error with the given tag. */
  public static void wtf(String tag, String message) {
    wtf(tag, message, null);
  }

  /** Log an unexpected error with the given tag and Throwable. */
  public static void wtf(String tag, String message, @Nullable Throwable cause) {
    if (sDelegate != null) {
      sDelegate.wtf(tag, message, cause);
    } else {
      Log.wtf(tag, message, cause);
    }
  }
}
