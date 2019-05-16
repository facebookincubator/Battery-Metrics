/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.appwakeup;

import static com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupDetails;
import static com.facebook.battery.metrics.appwakeup.AppWakeupMetrics.WakeupReason;
import static org.assertj.core.api.Java6Assertions.assertThat;

import androidx.collection.SimpleArrayMap;
import com.facebook.battery.metrics.core.SystemMetricsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AppWakeupMetricsTest extends SystemMetricsTest<AppWakeupMetrics> {

  @Override
  protected Class<AppWakeupMetrics> getClazz() {
    return AppWakeupMetrics.class;
  }

  @Test
  @Override
  public void testSum() throws Exception {
    AppWakeupMetrics a = getAppWakeupMetrics(4, 0);
    AppWakeupMetrics b = getAppWakeupMetrics(7, 10);
    AppWakeupMetrics output = new AppWakeupMetrics();
    a.sum(b, output);
    assertThat(output.appWakeups.size()).isEqualTo(7);
    verifyKeyAndReason(output.appWakeups);
    verifyWakeupDetails(output.appWakeups.valueAt(0), 10, 12);
    verifyWakeupDetails(output.appWakeups.valueAt(1), 12, 14);
    verifyWakeupDetails(output.appWakeups.valueAt(2), 14, 16);
    verifyWakeupDetails(output.appWakeups.valueAt(3), 16, 18);
    verifyWakeupDetails(output.appWakeups.valueAt(4), 14, 15);
    verifyWakeupDetails(output.appWakeups.valueAt(5), 15, 16);
    verifyWakeupDetails(output.appWakeups.valueAt(6), 16, 17);
  }

  @Test
  @Override
  public void testDiff() throws Exception {
    AppWakeupMetrics a = getAppWakeupMetrics(7, 5);
    AppWakeupMetrics b = getAppWakeupMetrics(4, 10);
    AppWakeupMetrics output = new AppWakeupMetrics();
    b.diff(a, output);

    assertThat(output.appWakeups.size()).isEqualTo(4);
    verifyKeyAndReason(output.appWakeups);
    verifyWakeupDetails(output.appWakeups.valueAt(0), 5, 5);
    verifyWakeupDetails(output.appWakeups.valueAt(1), 5, 5);
    verifyWakeupDetails(output.appWakeups.valueAt(2), 5, 5);
    verifyWakeupDetails(output.appWakeups.valueAt(3), 5, 5);
  }

  // Create a AppWakeupMetrics with size = numWakeups, with odd numbered wakeups as JS and
  // even numbered ones as Alarms.
  private static AppWakeupMetrics getAppWakeupMetrics(int numWakeups, int offset) {
    AppWakeupMetrics metrics = new AppWakeupMetrics();
    for (int i = 0; i < numWakeups; i++) {
      String key = "key-" + i;
      WakeupReason reason = (i % 2 == 0) ? WakeupReason.ALARM : WakeupReason.JOB_SCHEDULER;
      metrics.appWakeups.put(key, new WakeupDetails(reason, i + offset, i + offset + 1));
    }
    return metrics;
  }

  private static void verifyKeyAndReason(SimpleArrayMap<String, WakeupDetails> map) {
    for (int i = 0; i < map.size(); i++) {
      assertThat(map.keyAt(i)).isEqualTo("key-" + i);
      assertThat(map.valueAt(i).reason)
          .isEqualTo(i % 2 == 0 ? WakeupReason.ALARM : WakeupReason.JOB_SCHEDULER);
    }
  }

  private static void verifyWakeupDetails(WakeupDetails details, int count, int time) {
    assertThat(details.count).isEqualTo(count);
    assertThat(details.wakeupTimeMs).isEqualTo(time);
  }
}
