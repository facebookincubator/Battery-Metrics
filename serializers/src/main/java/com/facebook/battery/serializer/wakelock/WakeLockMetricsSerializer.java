/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.wakelock;

import com.facebook.battery.metrics.wakelock.WakeLockMetrics;
import com.facebook.battery.serializer.common.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class WakeLockMetricsSerializer extends SystemMetricsSerializer<WakeLockMetrics> {

  @Override
  public void serializeContents(WakeLockMetrics metrics, DataOutput output) throws IOException {
    output.writeLong(metrics.heldTimeMs);
    output.writeLong(metrics.acquiredCount);
    output.writeBoolean(metrics.isAttributionEnabled);
    if (metrics.isAttributionEnabled) {
      int size = metrics.tagTimeMs.size();
      output.writeInt(size);
      for (int i = 0; i < size; i++) {
        String key = metrics.tagTimeMs.keyAt(i);
        long value = metrics.tagTimeMs.valueAt(i);
        output.writeInt(key.length());
        output.writeChars(key);
        output.writeLong(value);
      }
    }
  }

  @Override
  public boolean deserializeContents(WakeLockMetrics metrics, DataInput input) throws IOException {
    metrics.tagTimeMs.clear();

    metrics.heldTimeMs = input.readLong();
    metrics.acquiredCount = input.readLong();
    metrics.isAttributionEnabled = input.readBoolean();
    if (metrics.isAttributionEnabled) {
      int size = input.readInt();
      for (int i = 0; i < size; i++) {
        int keySize = input.readInt();
        StringBuilder keyBuilder = new StringBuilder();
        for (int j = 0; j < keySize; j++) {
          keyBuilder.append(input.readChar());
        }
        metrics.tagTimeMs.put(keyBuilder.toString(), input.readLong());
      }
    }

    return true;
  }
}
