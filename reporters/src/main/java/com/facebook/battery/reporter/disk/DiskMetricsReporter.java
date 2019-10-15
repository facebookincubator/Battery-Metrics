/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.reporter.disk;

import com.facebook.battery.metrics.disk.DiskMetrics;
import com.facebook.battery.reporter.core.SystemMetricsReporter;

public class DiskMetricsReporter implements SystemMetricsReporter<DiskMetrics> {

  public static final String RCHAR_BYTES = "rchar_bytes";
  public static final String WCHAR_BYTES = "wchar_bytes";
  public static final String SYSCR_COUNT = "syscr_count";
  public static final String SYSCW_COUNT = "syscw_count";
  public static final String READ_BYTES = "read_bytes";
  public static final String WRITE_BYTES = "write_bytes";
  public static final String CANCELLED_WRITE_BYTES = "cancelled_write_bytes";

  @Override
  public void reportTo(DiskMetrics metrics, SystemMetricsReporter.Event event) {
    if (metrics.rcharBytes != 0) {
      event.add(RCHAR_BYTES, metrics.rcharBytes);
    }

    if (metrics.wcharBytes != 0) {
      event.add(WCHAR_BYTES, metrics.wcharBytes);
    }

    if (metrics.syscrCount != 0) {
      event.add(SYSCR_COUNT, metrics.syscrCount);
    }

    if (metrics.syscwCount != 0) {
      event.add(SYSCW_COUNT, metrics.syscwCount);
    }

    if (metrics.readBytes != 0) {
      event.add(READ_BYTES, metrics.readBytes);
    }

    if (metrics.writeBytes != 0) {
      event.add(WRITE_BYTES, metrics.writeBytes);
    }

    if (metrics.cancelledWriteBytes != 0) {
      event.add(CANCELLED_WRITE_BYTES, metrics.cancelledWriteBytes);
    }
  }
}
