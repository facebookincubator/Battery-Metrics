/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.core;

import android.os.Debug;

@org.robolectric.annotation.Implements(Debug.class)
public class ShadowDebug {
  private static long sNativeHeapSize;
  private static long sNativeHeapAllocatedSize;

  public static void setNativeHeapSize(long bytes) {
    sNativeHeapSize = bytes;
  }

  public static void setNativeHeapAllocatedSize(long bytes) {
    sNativeHeapAllocatedSize = bytes;
  }

  @org.robolectric.annotation.Implementation
  public static long getNativeHeapSize() {
    return sNativeHeapSize;
  }

  @org.robolectric.annotation.Implementation
  public static long getNativeHeapAllocatedSize() {
    return sNativeHeapAllocatedSize;
  }
}
