/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.core;

import androidx.annotation.Nullable;
import java.io.Serializable;

/**
 * Represents a simple bag of values. SystemMetrics expose all their values as public members, and
 * are generally meant to be recycled if used in hot paths.
 *
 * <p>The are simply monoids, and can be added/subtracted with utility functions to reset.
 */
public abstract class SystemMetrics<T extends SystemMetrics<T>> implements Serializable {

  /**
   * Add values from the SystemMetrics object passed in and sets them on the output object, if
   * available. Does not modify the original object.
   *
   * <p>For convenience, this function accepts null values and will allocate an output object if
   * none is passed in.
   *
   * @param b
   * @param output
   * @return output set to (this + b)
   */
  public abstract T sum(@Nullable T b, @Nullable T output);

  /**
   * Subtract the object passed in from the current object. Does not modify the original object.
   *
   * <p>For convenience, this function accepts null values and will allocate an output object if
   * none is passed in.
   *
   * @param b
   * @param output
   * @return output set to (this - b)
   */
  public abstract T diff(@Nullable T b, @Nullable T output);

  /**
   * Sets all fields in this to values from the SystemMetrics object passed in: a _deep_ copy that
   * shouldn't share any values with other metrics objects.
   *
   * @param b
   * @return this
   */
  public abstract T set(T b);

  /**
   * Convenience wrapper over {@link SystemMetrics#sum(SystemMetrics, SystemMetrics)} which always
   * allocates a new output object.
   *
   * @param b
   * @return this + b
   */
  public T sum(@Nullable T b) {
    return sum(b, null);
  }

  /**
   * Convenience wrapper over {@link SystemMetrics#diff(SystemMetrics, SystemMetrics)} which always
   * allocates a new output object.
   *
   * @param b
   * @return this - b
   */
  public T diff(@Nullable T b) {
    return diff(b, null);
  }
}
