/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.disk;

import static com.facebook.battery.metrics.core.Utilities.checkNotNull;

import androidx.annotation.GuardedBy;
import com.facebook.battery.metrics.core.ProcFileReader;
import com.facebook.battery.metrics.core.SystemMetricsCollector;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.infer.annotation.ThreadSafe;
import java.nio.CharBuffer;

@ThreadSafe
public class DiskMetricsCollector extends SystemMetricsCollector<DiskMetrics> {
  private static final String TAG = "DiskMetricsCollector";

  private static final int CHAR_BUFF_SIZE = 32;
  private static final String PROC_IO_FILE_PATH = "/proc/self/io";
  private final ThreadLocal<ProcFileReader> mProcIoFileReader = new ThreadLocal<>();

  private static final int PROC_STAT_MAJOR_FAULTS_FIELD = 11;
  private static final int PROC_STAT_BLKIO_TICKS_FIELD = 41;

  private static final String PROC_STAT_FILE_PATH = "/proc/self/stat";
  private final ThreadLocal<ProcFileReader> mProcStatFileReader = new ThreadLocal<>();

  @GuardedBy("this")
  private boolean mIsEnabled = false;

  @GuardedBy("this")
  private boolean mIsFirstSnapShot = true;

  @Override
  @ThreadSafe(enableChecks = false)
  public synchronized boolean getSnapshot(DiskMetrics snapshot) {
    checkNotNull(snapshot, "Null value passed to getSnapshot!");
    if (!mIsEnabled) {
      return false;
    }

    try {
      ProcFileReader ioReader = getIoFileReader();
      ProcFileReader statReader = getStatFileReader();

      ioReader.reset();
      statReader.reset();
      if (!ioReader.isValid() || !statReader.isValid()) {
        return false;
      }

      if (mIsFirstSnapShot) {
        mIsFirstSnapShot = false;
        return true;
      }

      snapshot.rcharBytes = readField(ioReader);
      snapshot.wcharBytes = readField(ioReader);
      snapshot.syscrCount = readField(ioReader);
      snapshot.syscwCount = readField(ioReader);
      snapshot.readBytes = readField(ioReader);
      snapshot.writeBytes = readField(ioReader);
      snapshot.cancelledWriteBytes = readField(ioReader);

      int index = 0;
      while (index < PROC_STAT_MAJOR_FAULTS_FIELD) {
        statReader.skipSpaces();
        index++;
      }

      snapshot.majorFaults = statReader.readNumber();

      while (index < PROC_STAT_BLKIO_TICKS_FIELD) {
        statReader.skipSpaces();
        index++;
      }

      snapshot.blkIoTicks = statReader.readNumber();
    } catch (ProcFileReader.ParseException pe) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse disk field", pe);
      return false;
    }

    return true;
  }

  private ProcFileReader getIoFileReader() {
    ProcFileReader ioReader = mProcIoFileReader.get();
    if (ioReader == null) {
      ioReader = new ProcFileReader(getIoFilePath());
      mProcIoFileReader.set(ioReader);
    }

    return ioReader;
  }

  private ProcFileReader getStatFileReader() {
    ProcFileReader statReader = mProcStatFileReader.get();
    if (statReader == null) {
      statReader = new ProcFileReader(getStatFilePath());
      mProcStatFileReader.set(statReader);
    }

    return statReader;
  }

  public synchronized void enable() {
    mIsEnabled = true;
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

  protected String getIoFilePath() {
    return PROC_IO_FILE_PATH;
  }

  protected String getStatFilePath() {
    return PROC_STAT_FILE_PATH;
  }

  public static boolean isSupported() {
    try {
      ProcFileReader reader = new ProcFileReader(PROC_IO_FILE_PATH);
      reader.reset();
      boolean supported = reader.isValid();
      reader.close();
      return supported;
    } catch (ProcFileReader.ParseException pe) {
      return false;
    }
  }
}
