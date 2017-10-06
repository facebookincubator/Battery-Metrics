// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.api;

import android.support.annotation.Nullable;
import android.util.Log;

public final class SystemMetricsLogger {

  public interface Delegate {
    void wtf(String Tag, String message, @Nullable Throwable cause);
  }

  private static Delegate sDelegate = null;

  public static void setDelegate(Delegate delegate) {
    sDelegate = delegate;
  }

  public static void wtf(String tag, String message) {
    wtf(tag, message, null);
  }

  public static void wtf (String tag, String message, @Nullable Throwable cause) {
    if (sDelegate != null) {
      sDelegate.wtf(tag, message, cause);
    } else {
      Log.wtf(tag, message, cause);
    }
  }
}
