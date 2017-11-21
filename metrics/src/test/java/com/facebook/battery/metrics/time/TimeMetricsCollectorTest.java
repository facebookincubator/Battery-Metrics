/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.time;

import static org.assertj.core.api.Java6Assertions.assertThat;

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

  @Override
  protected Class<TimeMetricsCollector> getClazz() {
    return TimeMetricsCollector.class;
  }
}
