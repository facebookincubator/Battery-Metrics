/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.appwakeup;

import static com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupDetails;
import static com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupReason;
import static org.assertj.core.api.Java6Assertions.assertThat;

import com.facebook.battery.metrics.common.ShadowSystemClock;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowSystemClock.class})
public class AppWakeupMetricsCollectorTest {

  private AppWakeupMetricsCollector mAppWakeupMetricsCollector;
  private AppWakeupMetrics mAppWakeupMetrics;

  @Before
  public void setUp() throws IOException {
    mAppWakeupMetricsCollector = new AppWakeupMetricsCollector();
    mAppWakeupMetrics = new AppWakeupMetrics();
  }

  @Test
  public void testGetSnapshot() {
    // Test with 4 open wakeups
    ShadowSystemClock.setElapsedRealtime(1);
    mAppWakeupMetricsCollector.recordWakeupStart(AppWakeupMetrics.WakeupReason.ALARM, "key1");
    ShadowSystemClock.setElapsedRealtime(2);
    mAppWakeupMetricsCollector.recordWakeupStart(
        AppWakeupMetrics.WakeupReason.JOB_SCHEDULER, "key2");
    ShadowSystemClock.setElapsedRealtime(3);
    mAppWakeupMetricsCollector.recordWakeupStart(
        AppWakeupMetrics.WakeupReason.JOB_SCHEDULER, "key3");
    ShadowSystemClock.setElapsedRealtime(4);
    mAppWakeupMetricsCollector.recordWakeupStart(AppWakeupMetrics.WakeupReason.ALARM, "key4");
    mAppWakeupMetricsCollector.getSnapshot(mAppWakeupMetrics);
    assertThat(mAppWakeupMetrics.appWakeups.size()).isEqualTo(0);

    // Test with 2 wakeups closed and 2 open
    ShadowSystemClock.setElapsedRealtime(20);
    mAppWakeupMetricsCollector.recordWakeupEnd("key1");
    ShadowSystemClock.setElapsedRealtime(30);
    mAppWakeupMetricsCollector.recordWakeupEnd("key3");
    ShadowSystemClock.setElapsedRealtime(35);
    mAppWakeupMetricsCollector.getSnapshot(mAppWakeupMetrics);
    assertThat(mAppWakeupMetrics.appWakeups.size()).isEqualTo(2);
    assertThat(mAppWakeupMetrics.appWakeups.get("key1"))
        .isEqualTo(new WakeupDetails(WakeupReason.ALARM, 1, 19));
    assertThat(mAppWakeupMetrics.appWakeups.get("key3"))
        .isEqualTo(new WakeupDetails(WakeupReason.JOB_SCHEDULER, 1, 27));

    // Test with 3 closed wakeups, with one wakeup being closed 2 times
    ShadowSystemClock.setElapsedRealtime(41);
    mAppWakeupMetricsCollector.recordWakeupStart(AppWakeupMetrics.WakeupReason.ALARM, "key1");
    ShadowSystemClock.setElapsedRealtime(50);
    mAppWakeupMetricsCollector.recordWakeupEnd("key1");
    ShadowSystemClock.setElapsedRealtime(57);
    mAppWakeupMetricsCollector.recordWakeupEnd("key2");

    mAppWakeupMetricsCollector.getSnapshot(mAppWakeupMetrics);
    assertThat(mAppWakeupMetrics.appWakeups.size()).isEqualTo(3);
    assertThat(mAppWakeupMetrics.appWakeups.get("key1"))
        .isEqualTo(new WakeupDetails(WakeupReason.ALARM, 2, 28));
    assertThat(mAppWakeupMetrics.appWakeups.get("key2"))
        .isEqualTo(new WakeupDetails(WakeupReason.JOB_SCHEDULER, 1, 55));
    assertThat(mAppWakeupMetrics.appWakeups.get("key3"))
        .isEqualTo(new WakeupDetails(WakeupReason.JOB_SCHEDULER, 1, 27));

    // Test that opening a open wakeup and closing a closed wakeup has no effect
    ShadowSystemClock.setElapsedRealtime(65);
    mAppWakeupMetricsCollector.recordWakeupStart(AppWakeupMetrics.WakeupReason.ALARM, "key4");
    ShadowSystemClock.setElapsedRealtime(65);
    mAppWakeupMetricsCollector.recordWakeupEnd("key1");
    mAppWakeupMetricsCollector.getSnapshot(mAppWakeupMetrics);
    assertThat(mAppWakeupMetrics.appWakeups.size()).isEqualTo(3);
    assertThat(mAppWakeupMetrics.appWakeups.get("key1"))
        .isEqualTo(new WakeupDetails(WakeupReason.ALARM, 2, 28));
    assertThat(mAppWakeupMetrics.appWakeups.get("key2"))
        .isEqualTo(new WakeupDetails(WakeupReason.JOB_SCHEDULER, 1, 55));
    assertThat(mAppWakeupMetrics.appWakeups.get("key3"))
        .isEqualTo(new WakeupDetails(WakeupReason.JOB_SCHEDULER, 1, 27));
  }
}
