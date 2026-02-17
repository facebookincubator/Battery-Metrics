/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.time;

import static org.assertj.core.api.Assertions.assertThat;

import com.facebook.battery.metrics.core.ShadowSystemClock;
import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(shadows = {ShadowSystemClock.class})
public class TimeMetricsCollectorTest
    extends SystemMetricsCollectorTest<TimeMetrics, TimeMetricsCollector> {

  @Test
  public void testTimes() {
    ShadowSystemClock.setUptimeMillis(1234);
    ShadowSystemClock.setElapsedRealtime(9876);
    TimeMetrics snapshot = new TimeMetrics();
    TimeMetricsCollector collector = new TimeMetricsCollector();
    collector.getSnapshot(snapshot);

    assertThat(snapshot.uptimeMs).isEqualTo(1234);
    assertThat(snapshot.realtimeMs).isEqualTo(9876);
  }

  @Test
  public void testBuggyOemUptimeNanosGetsCorrected() {
    // Simulate a buggy OEM device that returns nanoseconds from uptimeMillis().
    // Real uptime is 5 seconds: uptimeMillis() erroneously returns 5_000_000_000 (nanos).
    ShadowSystemClock.setUptimeMillis(5_000_000_000L);
    ShadowSystemClock.setElapsedRealtime(5000);
    TimeMetrics snapshot = new TimeMetrics();
    TimeMetricsCollector collector = new TimeMetricsCollector();
    collector.getSnapshot(snapshot);

    // uptimeMs (5_000_000_000) > realtimeMs * 1000 (5_000_000), so the collector divides by
    // 1_000_000
    assertThat(snapshot.realtimeMs).isEqualTo(5000);
    assertThat(snapshot.uptimeMs).isEqualTo(5000);
  }

  @Test
  public void testUptimeSlightlyExceedingRealtimeNotCorrected() {
    // Shortly after boot with minimal sleep, uptimeMs can legitimately be slightly larger
    // than realtimeMs due to the timing gap between elapsedRealtimeNanos() and uptimeMillis()
    // calls. The 1000x threshold should prevent this from being misidentified as a buggy device.
    ShadowSystemClock.setUptimeMillis(5002);
    ShadowSystemClock.setElapsedRealtime(5000);
    TimeMetrics snapshot = new TimeMetrics();
    TimeMetricsCollector collector = new TimeMetricsCollector();
    collector.getSnapshot(snapshot);

    assertThat(snapshot.realtimeMs).isEqualTo(5000);
    assertThat(snapshot.uptimeMs).isEqualTo(5002);
  }

  @Test
  public void testNormalUptimeLessThanRealtimeNotCorrected() {
    // Normal case: uptime (excludes sleep) is less than realtime (includes sleep).
    ShadowSystemClock.setUptimeMillis(3000);
    ShadowSystemClock.setElapsedRealtime(5000);
    TimeMetrics snapshot = new TimeMetrics();
    TimeMetricsCollector collector = new TimeMetricsCollector();
    collector.getSnapshot(snapshot);

    assertThat(snapshot.realtimeMs).isEqualTo(5000);
    assertThat(snapshot.uptimeMs).isEqualTo(3000);
  }

  @Override
  protected Class<TimeMetricsCollector> getClazz() {
    return TimeMetricsCollector.class;
  }
}
