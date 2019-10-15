/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.disk;

import static com.facebook.battery.metrics.core.Utilities.checkNotNull;

import com.facebook.battery.metrics.core.ProcFileReader;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.infer.annotation.ThreadSafe;
import java.nio.CharBuffer;

@ThreadSafe
public class DiskMetricsCollector extends SystemMetricsCollector<DiskMetrics> {
  private static final String TAG = "DiskMetricsCollector";

  private static final int CHAR_BUFF_SIZE = 32;
  private static final String PROC_STAT_FILE_PATH = "/proc/self/io";
  private final ThreadLocal<ProcFileReader> mProcFileReader = new ThreadLocal<>();

  @Override
  @ThreadSafe(enableChecks = false)
  public boolean getSnapshot(DiskMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");

    try {
      ProcFileReader reader = mProcFileReader.get();
      if (reader == null) {
        reader = new ProcFileReader(getPath());
        mProcFileReader.set(reader);
      }

      reader.reset();

      if (!reader.isValid()) {
        return false;
      }

      snapshot.rcharBytes = readField(reader);
      snapshot.wcharBytes = readField(reader);
      snapshot.syscrCount = readField(reader);
      snapshot.syscwCount = readField(reader);
      snapshot.readBytes = readField(reader);
      snapshot.writeBytes = readField(reader);
      snapshot.cancelledWriteBytes = readField(reader);

    } catch (ProcFileReader.ParseException pe) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse disk field", pe);
      return false;
    }

    return true;
  }

  @Override
  public DiskMetrics createMetrics() {
    return new DiskMetrics();
  }

  private static long readField(ProcFileReader reader) {
    reader.readWord(CharBuffer.allocate(CHAR_BUFF_SIZE));
    reader.skipSpaces();
    long count = reader.readNumber();
    reader.skipLine();
    return count;
  }

  protected String getPath() {
    return PROC_STAT_FILE_PATH;
  }

  public static boolean isSupported() {
    try {
      ProcFileReader reader = new ProcFileReader(PROC_STAT_FILE_PATH);
      reader.reset();
      boolean supported = reader.isValid();
      reader.close();
      return supported;
    } catch (ProcFileReader.ParseException pe) {
      return false;
    }
  }
}
