/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.core;

import com.facebook.battery.metrics.core.SystemMetrics;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * These help serialize SystemMetrics objects to disk cheaply as binary; using these helps avoid
 * reflection and unnecessary writes.
 */
public abstract class SystemMetricsSerializer<T extends SystemMetrics<T>> {

  private static final short MAGIC = 0xFB;
  private static final short VERSION = 1;

  /**
   * Serialize the complete metrics object with a tag that indicates the type of object serialized.
   */
  public final void serialize(T metrics, DataOutput output) throws IOException {
    output.writeShort(MAGIC);
    output.writeShort(VERSION);
    output.writeInt(getTag());
    serializeContents(metrics, output);
  }

  /** Deserialize the given object from the input stream. */
  public final boolean deserialize(T metrics, DataInput input) throws IOException {
    if (input.readShort() != MAGIC || input.readShort() != VERSION || input.readInt() != getTag()) {
      return false;
    }

    return deserializeContents(metrics, input);
  }

  /** Describes the class being encoded/decoded. */
  public final int getTag() {
    return getClass().toString().hashCode();
  }

  /** Must be implemented by every SystemMetricsSerializer to actually save data to output. */
  public abstract void serializeContents(T metrics, DataOutput output) throws IOException;

  /** Must be implemented by every SystemMetricsSerializer to read data from input. */
  public abstract boolean deserializeContents(T metrics, DataInput input) throws IOException;
}
