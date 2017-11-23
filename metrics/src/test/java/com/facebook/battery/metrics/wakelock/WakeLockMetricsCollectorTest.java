/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.wakelock;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import com.facebook.battery.metrics.core.ShadowSystemClock;
import com.facebook.battery.metrics.core.SystemMetricsCollectorTest;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowSystemClock.class})
public class WakeLockMetricsCollectorTest
    extends SystemMetricsCollectorTest<WakeLockMetrics, WakeLockMetricsCollector> {

  private PowerManager mPowerManager;
  private WakeLockMetricsCollector mCollector;

  @Before
  public void setUp() {
    mCollector = new WakeLockMetricsCollector();
    mPowerManager =
        (PowerManager) RuntimeEnvironment.application.getSystemService(Context.POWER_SERVICE);

    SystemMetricsLogger.setDelegate(
        new SystemMetricsLogger.Delegate() {
          @Override
          public void wtf(String tag, String message, @Nullable Throwable cause) {
            throw new RuntimeException(tag + " " + message, cause);
          }
        });
  }

  @Test
  public void testUnattributedSnapshot() {
    WakeLockMetrics metrics = new WakeLockMetrics();

    PowerManager.WakeLock wakelockA = mPowerManager.newWakeLock(0, "testA");
    mCollector.newWakeLock(wakelockA, 0, "testA");

    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Held time at beginning").isEqualTo(0);

    ShadowSystemClock.setUptimeMillis(1);
    mCollector.acquire(wakelockA, -1);

    ShadowSystemClock.setUptimeMillis(61);
    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Intermediate held time").isEqualTo(60);

    ShadowSystemClock.setUptimeMillis(91);
    mCollector.release(wakelockA, 0);

    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Held time at release").isEqualTo(90);

    ShadowSystemClock.setUptimeMillis(121);
    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Final held time").isEqualTo(90);
  }

  /**
   * Tests the lifetime of two overlapping wakelocks
   *
   * <pre>
   *   t = 000 001 031 061 091 121
   *   A =       [...........]
   *   B =           [...]
   * </pre>
   */
  @Test
  public void testAttributedSnapshot() {
    WakeLockMetrics metrics = new WakeLockMetrics(true);

    PowerManager.WakeLock wakeLockA = mPowerManager.newWakeLock(0, "testA");
    PowerManager.WakeLock wakeLockB = mPowerManager.newWakeLock(0, "testB");
    mCollector.newWakeLock(wakeLockA, 0, "testA");
    mCollector.newWakeLock(wakeLockB, 0, "testB");

    ShadowSystemClock.setUptimeMillis(1);
    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Initialization").isEqualTo(0);
    assertThat(metrics.tagTimeMs.isEmpty()).isFalse();
    assertThat(metrics.tagTimeMs.get("testA")).isEqualTo(0);
    assertThat(metrics.tagTimeMs.get("testB")).isEqualTo(0);

    ShadowSystemClock.setUptimeMillis(1);
    mCollector.acquire(wakeLockA, -1);
    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Acquired A").isEqualTo(0);

    ShadowSystemClock.setUptimeMillis(31);
    mCollector.acquire(wakeLockB, -1);
    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Acquired B").isEqualTo(30);
    assertThat(metrics.tagTimeMs.get("testA")).isEqualTo(30);
    assertThat(metrics.tagTimeMs.get("testB")).isEqualTo(0);

    ShadowSystemClock.setUptimeMillis(61);
    mCollector.release(wakeLockB, 0);
    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Released B").isEqualTo(60);
    assertThat(metrics.tagTimeMs.get("testA")).isEqualTo(60);
    assertThat(metrics.tagTimeMs.get("testB")).isEqualTo(30);

    ShadowSystemClock.setUptimeMillis(91);
    mCollector.release(wakeLockA, 0);
    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Released A").isEqualTo(90);
    assertThat(metrics.tagTimeMs.get("testA")).isEqualTo(90);
    assertThat(metrics.tagTimeMs.get("testB")).isEqualTo(30);

    ShadowSystemClock.setUptimeMillis(121);
    mCollector.getSnapshot(metrics);
    assertThat(metrics.heldTimeMs).as("Final").isEqualTo(90);
    assertThat(metrics.tagTimeMs.get("testA")).isEqualTo(90);
    assertThat(metrics.tagTimeMs.get("testB")).isEqualTo(30);
  }

  @Test
  public void testDisabling() {
    WakeLockMetrics metrics = new WakeLockMetrics(true);
    PowerManager.WakeLock wakeLockA = mPowerManager.newWakeLock(0, "testA");
    mCollector.newWakeLock(wakeLockA, 0, "testA");

    assertThat(mCollector.getSnapshot(metrics)).isTrue();

    mCollector.disable();
    assertThat(mCollector.getSnapshot(metrics)).isFalse();

    // Sanity check that nothing throws an exception or logs after disabling
    mCollector.release(wakeLockA, 0);
    mCollector.acquire(wakeLockA, 100);
  }

  @Override
  protected Class<WakeLockMetricsCollector> getClazz() {
    return WakeLockMetricsCollector.class;
  }
}
