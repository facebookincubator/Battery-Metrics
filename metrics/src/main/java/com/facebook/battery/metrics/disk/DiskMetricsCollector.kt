/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.disk

import androidx.annotation.GuardedBy
import com.facebook.battery.metrics.core.ProcFileReader
import com.facebook.battery.metrics.core.SystemMetricsCollector
import com.facebook.battery.metrics.core.SystemMetricsLogger
import com.facebook.battery.metrics.core.Utilities
import com.facebook.infer.annotation.ThreadSafe
import java.nio.CharBuffer

@ThreadSafe
open class DiskMetricsCollector : SystemMetricsCollector<DiskMetrics?>() {

  private val procIoFileReader = ThreadLocal<ProcFileReader>()

  private val procStatFileReader = ThreadLocal<ProcFileReader>()

  @GuardedBy("this") private var isEnabled = false

  @ThreadSafe(enableChecks = false)
  @Synchronized
  override fun getSnapshot(snapshot: DiskMetrics): Boolean {
    Utilities.checkNotNull(snapshot, "Null value passed to getSnapshot!")
    if (!isEnabled) {
      return false
    }

    try {
      var ioReader = procIoFileReader.get()
      if (ioReader == null) {
        ioReader = ProcFileReader(ioFilePath!!)
        procIoFileReader.set(ioReader)
      }

      ioReader.reset()

      if (!ioReader.isValid) {
        return false
      }

      snapshot.rcharBytes = readField(ioReader)
      snapshot.wcharBytes = readField(ioReader)
      snapshot.syscrCount = readField(ioReader)
      snapshot.syscwCount = readField(ioReader)
      snapshot.readBytes = readField(ioReader)
      snapshot.writeBytes = readField(ioReader)
      snapshot.cancelledWriteBytes = readField(ioReader)

      var statReader = procStatFileReader.get()
      if (statReader == null) {
        statReader = ProcFileReader(statFilePath!!)
        procStatFileReader.set(statReader)
      }

      statReader.reset()

      if (!statReader.isValid) {
        return false
      }

      var index = 0
      while (index < PROC_STAT_MAJOR_FAULTS_FIELD) {
        statReader.skipSpaces()
        index++
      }

      snapshot.majorFaults = statReader.readNumber()

      while (index < PROC_STAT_BLKIO_TICKS_FIELD) {
        statReader.skipSpaces()
        index++
      }

      snapshot.blkIoTicks = statReader.readNumber()
    } catch (pe: ProcFileReader.ParseException) {
      SystemMetricsLogger.wtf(TAG, "Unable to parse disk field", pe)
      return false
    }

    return true
  }

  @Synchronized
  fun enable() {
    isEnabled = true
  }

  override fun createMetrics(): DiskMetrics = DiskMetrics()

  protected open val ioFilePath: String?
    get() = PROC_IO_FILE_PATH

  protected open val statFilePath: String?
    get() = PROC_STAT_FILE_PATH

  companion object {
    private const val TAG = "DiskMetricsCollector"

    private const val CHAR_BUFF_SIZE = 32
    private const val PROC_IO_FILE_PATH = "/proc/self/io"
    private const val PROC_STAT_MAJOR_FAULTS_FIELD = 11
    private const val PROC_STAT_BLKIO_TICKS_FIELD = 41

    private const val PROC_STAT_FILE_PATH = "/proc/self/stat"

    private fun readField(reader: ProcFileReader): Long {
      reader.readWord(CharBuffer.allocate(CHAR_BUFF_SIZE))
      reader.skipSpaces()
      val count = reader.readNumber()
      reader.skipLine()
      return count
    }

    @get:JvmStatic
    val isSupported: Boolean
      get() {
        try {
          val reader = ProcFileReader(PROC_IO_FILE_PATH)
          reader.reset()
          val supported = reader.isValid
          reader.close()
          return supported
        } catch (pe: ProcFileReader.ParseException) {
          return false
        }
      }
  }
}
