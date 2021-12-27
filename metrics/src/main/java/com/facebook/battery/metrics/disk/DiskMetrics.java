/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.disk;

import androidx.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;

public class DiskMetrics extends SystemMetrics<DiskMetrics> {
  public long rcharBytes;
  public long wcharBytes;
  public long syscrCount;
  public long syscwCount;
  public long readBytes;
  public long writeBytes;
  public long cancelledWriteBytes;
  public long majorFaults;
  public long blkIoTicks;

  public DiskMetrics() {}

  @Override
  public DiskMetrics set(DiskMetrics metrics) {
    rcharBytes = metrics.rcharBytes;
    wcharBytes = metrics.wcharBytes;
    syscrCount = metrics.syscrCount;
    syscwCount = metrics.syscwCount;
    readBytes = metrics.readBytes;
    writeBytes = metrics.writeBytes;
    cancelledWriteBytes = metrics.cancelledWriteBytes;
    majorFaults = metrics.majorFaults;
    blkIoTicks = metrics.blkIoTicks;
    return this;
  }

  @Override
  public DiskMetrics sum(@Nullable DiskMetrics b, @Nullable DiskMetrics output) {
    if (output == null) {
      output = new DiskMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.rcharBytes = rcharBytes + b.rcharBytes;
      output.wcharBytes = wcharBytes + b.wcharBytes;
      output.syscrCount = syscrCount + b.syscrCount;
      output.syscwCount = syscwCount + b.syscwCount;
      output.readBytes = readBytes + b.readBytes;
      output.writeBytes = writeBytes + b.writeBytes;
      output.cancelledWriteBytes = cancelledWriteBytes + b.cancelledWriteBytes;
      output.majorFaults = majorFaults + b.majorFaults;
      output.blkIoTicks = blkIoTicks + b.blkIoTicks;
    }

    return output;
  }

  @Override
  public DiskMetrics diff(@Nullable DiskMetrics b, @Nullable DiskMetrics output) {
    if (output == null) {
      output = new DiskMetrics();
    }

    if (b == null) {
      output.set(this);
    } else {
      output.rcharBytes = rcharBytes - b.rcharBytes;
      output.wcharBytes = wcharBytes - b.wcharBytes;
      output.syscrCount = syscrCount - b.syscrCount;
      output.syscwCount = syscwCount - b.syscwCount;
      output.readBytes = readBytes - b.readBytes;
      output.writeBytes = writeBytes - b.writeBytes;
      /* cancelledWriteBytes can be -ve if the file deleted before flushed from file cache */
      output.cancelledWriteBytes = cancelledWriteBytes - b.cancelledWriteBytes;
      output.majorFaults = majorFaults - b.majorFaults;
      output.blkIoTicks = blkIoTicks - b.blkIoTicks;
    }

    return output;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    DiskMetrics that = (DiskMetrics) other;

    return that.rcharBytes == rcharBytes
        && that.wcharBytes == wcharBytes
        && that.syscrCount == syscrCount
        && that.syscwCount == syscwCount
        && that.readBytes == readBytes
        && that.writeBytes == writeBytes
        && that.cancelledWriteBytes == cancelledWriteBytes
        && that.majorFaults == majorFaults
        && that.blkIoTicks == blkIoTicks;
  }

  @Override
  public int hashCode() {
    int result = (int) (rcharBytes ^ (rcharBytes >>> 32));
    result = 31 * result + (int) (wcharBytes ^ (wcharBytes >>> 32));
    result = 31 * result + (int) (syscrCount ^ (syscrCount >>> 32));
    result = 31 * result + (int) (syscwCount ^ (syscwCount >>> 32));
    result = 31 * result + (int) (readBytes ^ (readBytes >>> 32));
    result = 31 * result + (int) (writeBytes ^ (writeBytes >>> 32));
    result = 31 * result + (int) (cancelledWriteBytes ^ (cancelledWriteBytes >>> 32));
    result = 31 * result + (int) (majorFaults ^ (majorFaults >>> 32));
    result = 31 * result + (int) (blkIoTicks ^ (blkIoTicks >>> 32));

    return result;
  }

  @Override
  public String toString() {
    return "DiskMetrics{"
        + "rcharBytes="
        + rcharBytes
        + ", wcharBytes="
        + wcharBytes
        + ", syscrCount="
        + syscrCount
        + ", syscwCount="
        + syscwCount
        + ", readBytes="
        + readBytes
        + ", writeBytes="
        + writeBytes
        + ", cancelledWriteBytes="
        + cancelledWriteBytes
        + ", majorFaults="
        + majorFaults
        + ", blkIoTicks="
        + blkIoTicks
        + "}";
  }
}
