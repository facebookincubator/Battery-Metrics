/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.core;

import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.util.SparseArray;

/**
 * A static class with a collection of utilities that didn't quite fit anywhere else.
 *
 * <p>These are lightweight, re-usable functions used across different metrics -- if a function
 * requires state, significant new dependencies or a collection of interlinked functions, consider
 * making it a separate module/package.
 */
public abstract class Utilities {

  /**
   * Check for equality between simple array maps: the equals function was broken in old versions of
   * the android support library.
   *
   * <p>This was fixed by commit goo.gl/nRWXvR
   */
  public static <K, V> boolean simpleArrayMapEquals(
      SimpleArrayMap<K, V> a, SimpleArrayMap<K, V> b) {
    if (a == b) {
      return true;
    }

    int aSize = a.size();
    int bSize = b.size();
    if (aSize != bSize) {
      return false;
    }

    for (int i = 0; i < aSize; i++) {
      K aKey = a.keyAt(i);
      V aValue = a.valueAt(i);

      V bValue = b.get(aKey);

      if (aValue == null) {
        if (bValue != null || !b.containsKey(aKey)) {
          return false;
        }
      } else if (!aValue.equals(bValue)) {
        return false;
      }
    }

    return true;
  }

  public static void checkNotNull(Object o, String message) {
    if (o == null) {
      throw new IllegalArgumentException(message);
    }
  }

  public static <V> boolean sparseArrayEquals(
      @Nullable SparseArray<V> a, @Nullable SparseArray<V> b) {
    if (a == b) {
      return true;
    }

    if (a == null || b == null) {
      return false;
    }

    if (a.size() != b.size()) {
      return false;
    }

    for (int i = 0; i < a.size(); i++) {
      int key = a.keyAt(i);
      if (!a.get(key).equals(b.get(key))) {
        return false;
      }
    }

    return true;
  }
}
