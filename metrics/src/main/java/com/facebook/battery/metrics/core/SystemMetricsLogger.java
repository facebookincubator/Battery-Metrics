/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.core;

import android.util.Log;
import androidx.annotation.Nullable;
import com.facebook.infer.annotation.Nullsafe;
import com.facebook.infer.annotation.ThreadSafe;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A utility class to collect logs from the battery metrics library: it's generally a good idea to
 * hook this up with error trace collection to sanity check against any errors in the Metrics
 * Collectors.
 */
@Nullsafe(Nullsafe.Mode.LOCAL)
@ThreadSafe
public final class SystemMetricsLogger {

  @ThreadSafe
  public interface Delegate {
    void wtf(String Tag, String message, @Nullable Throwable cause);
  }

  private static final AtomicReference<Delegate> DELEGATE = new AtomicReference<>();

  /**
   * Set a custom logging implementation: if there is none, then the library will simply log to
   * Logcat.
   */
  public static void setDelegate(Delegate delegate) {
    DELEGATE.set(delegate);
  }

  /** Log an unexpected error with the given tag. */
  public static void wtf(String tag, String message) {
    wtf(tag, message, null);
  }

  /** Log an unexpected error with the given tag and Throwable. */
  public static void wtf(String tag, String message, @Nullable Throwable cause) {
    Delegate delegate = DELEGATE.get();
    if (delegate != null) {
      delegate.wtf(tag, message, cause);
    } else {
      // This is explicitly `Log.e` to avoid possibly crashing the app.
      Log.e(tag, message, cause);
    }
  }
}
