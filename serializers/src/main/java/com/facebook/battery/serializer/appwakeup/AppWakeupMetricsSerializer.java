/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.serializer.appwakeup;

import static com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupDetails;
import static com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupReason;

import com.facebook.battery.metrics.appwakeup.AppWakeupMetrics;
import com.facebook.battery.serializer.core.SystemMetricsSerializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AppWakeupMetricsSerializer extends SystemMetricsSerializer<AppWakeupMetrics> {

  @Override
  public void serializeContents(AppWakeupMetrics metrics, DataOutput output) throws IOException {
    output.writeInt(metrics.appWakeups.size());
    for (int i = 0; i < metrics.appWakeups.size(); i++) {
      String wakeupName = metrics.appWakeups.keyAt(i);
      AppWakeupMetrics.WakeupDetails details = metrics.appWakeups.valueAt(i);
      output.writeInt(wakeupName.length());
      output.writeChars(wakeupName);
      output.writeInt(details.reason.ordinal());
      output.writeLong(details.count);
      output.writeLong(details.wakeupTimeMs);
    }
  }

  @Override
  public boolean deserializeContents(AppWakeupMetrics metrics, DataInput input) throws IOException {
    metrics.appWakeups.clear();
    int size = input.readInt();
    for (int i = 0; i < size; i++) {
      String wakeupName = readChars(input, input.readInt());
      WakeupReason reason = WakeupReason.values()[input.readInt()];
      long count = input.readLong();
      long timeMs = input.readLong();
      metrics.appWakeups.put(wakeupName, new WakeupDetails(reason, count, timeMs));
    }
    return true;
  }

  private String readChars(DataInput input, int len) throws IOException {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < len; i++) {
      builder.append(input.readChar());
    }
    return builder.toString();
  }
}
