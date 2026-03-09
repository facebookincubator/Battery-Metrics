/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.reporter.disk

import com.facebook.battery.metrics.disk.DiskMetrics
import com.facebook.battery.reporter.core.SystemMetricsReporter

class DiskMetricsReporter : SystemMetricsReporter<DiskMetrics> {

  override fun reportTo(metrics: DiskMetrics, event: SystemMetricsReporter.Event) {
    if (metrics.rcharBytes != 0L) {
      event.add(RCHAR_BYTES, metrics.rcharBytes)
    }

    if (metrics.wcharBytes != 0L) {
      event.add(WCHAR_BYTES, metrics.wcharBytes)
    }

    if (metrics.syscrCount != 0L) {
      event.add(SYSCR_COUNT, metrics.syscrCount)
    }

    if (metrics.syscwCount != 0L) {
      event.add(SYSCW_COUNT, metrics.syscwCount)
    }

    if (metrics.readBytes != 0L) {
      event.add(READ_BYTES, metrics.readBytes)
    }

    if (metrics.writeBytes != 0L) {
      event.add(WRITE_BYTES, metrics.writeBytes)
    }

    if (metrics.cancelledWriteBytes != 0L) {
      event.add(CANCELLED_WRITE_BYTES, metrics.cancelledWriteBytes)
    }

    if (metrics.majorFaults != 0L) {
      event.add(MAJOR_FAULTS_COUNT, metrics.majorFaults)
    }

    if (metrics.blkIoTicks != 0L) {
      event.add(BLK_IO_TICKS, metrics.blkIoTicks)
    }
  }

  companion object {
    const val RCHAR_BYTES: String = "rchar_bytes"
    const val WCHAR_BYTES: String = "wchar_bytes"
    const val SYSCR_COUNT: String = "syscr_count"
    const val SYSCW_COUNT: String = "syscw_count"
    const val READ_BYTES: String = "read_bytes"
    const val WRITE_BYTES: String = "write_bytes"
    const val CANCELLED_WRITE_BYTES: String = "cancelled_write_bytes"
    const val MAJOR_FAULTS_COUNT: String = "major_faults_count"
    const val BLK_IO_TICKS: String = "blk_io_ticks"
  }
}
