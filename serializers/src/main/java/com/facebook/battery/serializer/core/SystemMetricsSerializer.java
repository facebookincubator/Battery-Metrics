/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.serializer.core;

import com.facebook.battery.metrics.core.SystemMetrics;
import com.facebook.infer.annotation.Nullsafe;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * These help serialize SystemMetrics objects to disk cheaply as binary; using these helps avoid
 * reflection and unnecessary writes (as opposed to using Serializable).
 */
@Nullsafe(Nullsafe.Mode.LOCAL)
public abstract class SystemMetricsSerializer<T extends SystemMetrics<T>> {

  private static final short MAGIC = 0xFB;
  private static final short VERSION = 2;

  /**
   * Serialize the complete metrics object with a tag that indicates the type of object serialized.
   */
  public final void serialize(T metrics, DataOutput output) throws IOException {
    output.writeShort(MAGIC);
    output.writeShort(VERSION);
    output.writeLong(getTag());
    serializeContents(metrics, output);
  }

  /** Deserialize the given object from the input stream. */
  public final boolean deserialize(T metrics, DataInput input) throws IOException {
    if (input.readShort() != MAGIC
        || input.readShort() != VERSION
        || input.readLong() != getTag()) {
      return false;
    }

    return deserializeContents(metrics, input);
  }

  /**
   * Identifies the class being encoded/decoded: this MUST be unique per serializer.
   *
   * <p>A convenient way to generate these is to simply use serialver -- similar to Serializable
   */
  public abstract long getTag();

  /** Must be implemented by every SystemMetricsSerializer to actually save data to output. */
  public abstract void serializeContents(T metrics, DataOutput output) throws IOException;

  /** Must be implemented by every SystemMetricsSerializer to read data from input. */
  public abstract boolean deserializeContents(T metrics, DataInput input) throws IOException;
}
