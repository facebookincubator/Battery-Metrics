// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.api;

import android.support.annotation.Nullable;
import java.io.Serializable;

public abstract class SystemMetrics<T extends SystemMetrics<T>> implements Serializable {

  /**
   * Add values from the SystemMetrics object passed in and sets them on the output object,
   * if available. Does not modify the original object.
   *
   * For convenience, this function accepts null values and will allocate an output object if none
   * is passed in.
   * @param b
   * @param output
   * @return output set to (this + b)
   */
  abstract public T sum(@Nullable T b, @Nullable T output);

  /**
   * Subtract the object passed in from the current object. Does not modify the original object.
   *
   * For convenience, this function accepts null values and will allocate an output object if none
   * is passed in.
   * @param b
   * @param output
   * @return output set to (this - b)
   */
  abstract public T diff(@Nullable T b, @Nullable T output);

  /**
   * Sets all fields in this to values from the SystemMetrics object passed in: a deep copy.
   *
   * @param b
   * @return this
   */
  abstract public T set(T b);

  /**
   * Convenience wrapper over {@link SystemMetrics#sum(SystemMetrics, SystemMetrics)} which always
   * allocates a new output object.
   * @param b
   * @return this + b
   */
  public T sum(@Nullable T b) {
    return sum(b, null);
  }

  /**
   * Convenience wrapper over {@link SystemMetrics#diff(SystemMetrics, SystemMetrics)} which always
   * allocates a new output object.
   * @param b
   * @return this - b
   */
  public T diff(@Nullable T b) {
    return diff(b, null);
  }
}
