/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.network;

import static java.lang.Math.max;

import com.facebook.infer.annotation.ThreadSafe;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for keeping track of total radio transfer time and tail time. It is assumed that
 * {@link #onRadioActivate(long, long)} will be called with monotonically increasing transfer start
 * values.
 */
@ThreadSafe
public class MonotonicRadioMonitor {

  private final int mWakeUpTimeS;

  // packed next second when it becomes idle (32bit),
  // total transfer seconds (16bits), and total tail seconds (16 bits) (at next idle)
  final AtomicLong mNextIdleTimeActive = new AtomicLong();
  final AtomicInteger mWakeupCounter = new AtomicInteger();

  public MonotonicRadioMonitor(int wakeUpTimeS) {
    mWakeUpTimeS = wakeUpTimeS;
  }

  public int onRadioActivate(long transferStartMs, long transferEndMs) {
    long transferStartS = TimeUnit.MILLISECONDS.toSeconds(transferStartMs);
    long transferEndS = TimeUnit.MILLISECONDS.toSeconds(transferEndMs);
    long expectedNextIdleAndTotals;
    long newNextIdleAndTotals;
    boolean casSucceeded = false;

    /*
     * Theoretically, it's possible that a call to this function could be made,
     * not be scheduled for about 10 seconds, and then another call could be scheduled before it,
     * meaning that a wakeup/active time that occurred would not be counted.
     * Realistically, the odds of a thread going 10 seconds without being scheduled are fairly
     * low so it's not a huge worry.
     */

    do {
      expectedNextIdleAndTotals = mNextIdleTimeActive.get();
      newNextIdleAndTotals =
          adjustTotalsAndNextIdle(transferStartS, transferEndS, expectedNextIdleAndTotals);
    } while (nextIdle(expectedNextIdleAndTotals) < nextIdle(newNextIdleAndTotals)
        && !(casSucceeded =
            mNextIdleTimeActive.compareAndSet(expectedNextIdleAndTotals, newNextIdleAndTotals)));

    if (casSucceeded) {
      if (nextIdle(expectedNextIdleAndTotals) <= transferStartS) {
        mWakeupCounter.getAndIncrement();
      }
      return totalTxS(expectedNextIdleAndTotals) + totalTailS(expectedNextIdleAndTotals);
    } else {
      return 0;
    }
  }

  private long adjustTotalsAndNextIdle(
      long transferStartS, long transferEndS, long nextIdleAndTotals) {
    long nextIdle = nextIdle(nextIdleAndTotals);
    long oldTransferEnd = nextIdle - mWakeUpTimeS;
    // If transfer end goes into new second we want at least one second for transfer
    long transferDeltaAdjustment = transferEndS > oldTransferEnd ? 1L : 0L;
    transferStartS = max(transferStartS, oldTransferEnd);
    transferEndS = max(transferEndS, oldTransferEnd);

    long oldTransferTotal = totalTxS(nextIdleAndTotals);
    long oldTailTotal = totalTailS(nextIdleAndTotals);

    long transferDelta = max(transferEndS - transferStartS, transferDeltaAdjustment);

    long tailDelta;
    if (transferStartS < nextIdle) {
      tailDelta =
          (transferEndS < nextIdle)
              ? mWakeUpTimeS - transferDelta - (nextIdle - transferEndS)
              : mWakeUpTimeS - (nextIdle - transferStartS);
    } else {
      tailDelta = mWakeUpTimeS;
    }

    return makeIdleValue(
        transferEndS + mWakeUpTimeS, oldTransferTotal + transferDelta, oldTailTotal + tailDelta);
  }

  public long getTotalTxS() {
    return totalTxS(mNextIdleTimeActive.get());
  }

  public long getTotalTailS() {
    return totalTailS(mNextIdleTimeActive.get());
  }

  public long getWakeupCount() {
    return mWakeupCounter.get();
  }

  static long makeIdleValue(long nextIdleS, long totalTransferS, long totalTailS) {
    return (nextIdleS << 32) | (totalTransferS << 16) | totalTailS;
  }

  static long nextIdle(long nextIdleTimeActive) {
    return nextIdleTimeActive >> 32;
  }

  static int totalTxS(long nextIdleTimeActive) {
    return (int) ((nextIdleTimeActive & 0xFFFF0000L) >> 16);
  }

  static int totalTailS(long nextIdleTimeActive) {
    return (int) (nextIdleTimeActive & 0xFFFFL);
  }
}
