/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.network;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MonotonicRadioMonitorTest {

  private static final int WAKEUP_INTERVAL_S = 10;

  @Test
  public void testRadioNeverActive() throws Exception {
    MonotonicRadioMonitor radioMonitor = new MonotonicRadioMonitor(WAKEUP_INTERVAL_S);

    final int radioActiveS = getRadioActiveS(radioMonitor);
    assertThat(radioActiveS).isEqualTo(0);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(0);
  }

  @Test
  public void testSingleRadioActive() throws Exception {
    MonotonicRadioMonitor radioMonitor = new MonotonicRadioMonitor(WAKEUP_INTERVAL_S);

    int radioActiveS = radioMonitor.onRadioActivate(0L, 3000L);

    assertThat(radioActiveS).isEqualTo(0);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(3 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);
  }

  @Test
  public void testMultipleSequentialRadioActive() throws Exception {
    MonotonicRadioMonitor radioMonitor = new MonotonicRadioMonitor(WAKEUP_INTERVAL_S);

    int firstRadioActiveS = radioMonitor.onRadioActivate(0L, 2000L);
    assertThat(firstRadioActiveS).isEqualTo(0);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(2 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);

    int secondRadioActiveS = radioMonitor.onRadioActivate(2000L, 3000L);
    assertThat(secondRadioActiveS).isEqualTo(2 + WAKEUP_INTERVAL_S);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(3 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);

    int thirdRadioActiveS = radioMonitor.onRadioActivate(3000L, 4000L);
    assertThat(thirdRadioActiveS).isEqualTo(3 + WAKEUP_INTERVAL_S);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(4 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);
  }

  @Test
  public void testMultipleSequentialAfterTailRadioActive() throws Exception {
    MonotonicRadioMonitor radioMonitor = new MonotonicRadioMonitor(WAKEUP_INTERVAL_S);

    int firstRadioActiveS = radioMonitor.onRadioActivate(0L, 2000L);
    assertThat(firstRadioActiveS).isEqualTo(0);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(2 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);

    long delay = TimeUnit.SECONDS.toMillis(WAKEUP_INTERVAL_S);
    int secondRadioActiveS = radioMonitor.onRadioActivate(2000L + delay, 3000L + delay);
    assertThat(secondRadioActiveS).isEqualTo(2 + WAKEUP_INTERVAL_S);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(3 + 2 * WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(2);

    int thirdRadioActiveS = radioMonitor.onRadioActivate(3000L + 2 * delay, 4000L + 2 * delay);
    assertThat(thirdRadioActiveS).isEqualTo(3 + 2 * WAKEUP_INTERVAL_S);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(4 + 3 * WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(3);
  }

  @Test
  public void testMultipleParallelRadioActiveFirstEndBeforeSecond() throws Exception {
    MonotonicRadioMonitor radioMonitor = new MonotonicRadioMonitor(WAKEUP_INTERVAL_S);

    int firstRadioActiveS = radioMonitor.onRadioActivate(1000L, 2000L);
    assertThat(firstRadioActiveS).isEqualTo(0);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(1 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);

    int secondRadioActiveS = radioMonitor.onRadioActivate(2000L, 3000L);
    assertThat(secondRadioActiveS).isEqualTo(1 + WAKEUP_INTERVAL_S);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(2 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);
  }

  @Test
  public void testMultipleParallelRadioActiveFirstEndWithSecond() throws Exception {
    MonotonicRadioMonitor radioMonitor = new MonotonicRadioMonitor(WAKEUP_INTERVAL_S);

    int firstRadioActiveS = radioMonitor.onRadioActivate(1000L, 3000L);
    assertThat(firstRadioActiveS).isEqualTo(0);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(2 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);

    int secondRadioActiveS = radioMonitor.onRadioActivate(2000L, 3000L);
    assertThat(secondRadioActiveS).isEqualTo(0);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(2 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);
  }

  @Test
  public void testMultipleParallelRadioActiveFirstEndAfterSecond() throws Exception {
    MonotonicRadioMonitor radioMonitor = new MonotonicRadioMonitor(WAKEUP_INTERVAL_S);

    int firstRadioActiveS = radioMonitor.onRadioActivate(1000L, 4000L);
    assertThat(firstRadioActiveS).isEqualTo(0);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(3 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);

    int secondRadioActiveS = radioMonitor.onRadioActivate(2000L, 3000L);
    assertThat(secondRadioActiveS).isEqualTo(0);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(3 + WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(1);
  }

  @Test
  public void testMixedRadioActive() throws Exception {
    MonotonicRadioMonitor radioMonitor = new MonotonicRadioMonitor(WAKEUP_INTERVAL_S);

    radioMonitor.onRadioActivate(1000L, 4000L);
    radioMonitor.onRadioActivate(2000L, 3000L);

    radioMonitor.onRadioActivate(15000L, 18000L);
    radioMonitor.onRadioActivate(16000L, 18000L);

    int radioActiveS = radioMonitor.onRadioActivate(29000L, 30000L);

    assertThat(radioActiveS).isEqualTo(6 + 2 * WAKEUP_INTERVAL_S);
    assertThat(getRadioActiveS(radioMonitor)).isEqualTo(7 + 3 * WAKEUP_INTERVAL_S);
    assertThat(radioMonitor.mWakeupCounter.get()).isEqualTo(3);
  }

  private static int getRadioActiveS(MonotonicRadioMonitor radioMonitor) {
    final long totals = radioMonitor.mNextIdleTimeActive.get();
    return MonotonicRadioMonitor.totalTxS(totals) + MonotonicRadioMonitor.totalTailS(totals);
  }
}
