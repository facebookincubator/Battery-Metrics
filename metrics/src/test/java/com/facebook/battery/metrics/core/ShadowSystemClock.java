/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.core;

import android.os.SystemClock;

@org.robolectric.annotation.Implements(SystemClock.class)
public class ShadowSystemClock {

  private static long sElapsedRealtime;
  private static long sUptimeMillis;

  public static void setUptimeMillis(long ms) {
    sUptimeMillis = ms;
  }

  public static void setElapsedRealtime(long ms) {
    sElapsedRealtime = ms;
  }

  @org.robolectric.annotation.Implementation
  public static long uptimeMillis() {
    return sUptimeMillis;
  }

  @org.robolectric.annotation.Implementation
  public static long elapsedRealtime() {
    return sElapsedRealtime;
  }
}
