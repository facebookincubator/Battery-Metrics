/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.wakelock;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.content.Context;
import android.os.PowerManager;
import com.facebook.battery.metrics.core.ShadowSystemClock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowSystemClock.class})
public class WakeLockDetailsTest {

  private PowerManager.WakeLock mWakeLock;

  @Before
  public void setUp() {
    PowerManager powerManager =
        (PowerManager) RuntimeEnvironment.application.getSystemService(Context.POWER_SERVICE);
    mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "batterytest:test");
  }

  @Test
  public void testAcquireReleaseReferenceCounted() {
    ShadowSystemClock.setUptimeMillis(1);
    WakeLockDetails details = new WakeLockDetails(mWakeLock, "test", 0);

    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();
    assertThat(details.getHeldTimeMs()).isEqualTo(0);

    ShadowSystemClock.setUptimeMillis(10);
    details.applyAutomaticReleases();
    details.release();
    assertThat(details.isHeld()).isFalse();
    assertThat(details.getHeldTimeMs()).isEqualTo(9);

    ShadowSystemClock.setUptimeMillis(100);
    details.applyAutomaticReleases();
    assertThat(details.isHeld()).isFalse();
    assertThat(details.getHeldTimeMs()).isEqualTo(9);
  }

  @Test
  public void testAcquireTimeoutReferenceCounted() {
    ShadowSystemClock.setUptimeMillis(1);
    WakeLockDetails details = new WakeLockDetails(mWakeLock, "test", 0);

    details.acquire(60);
    details.applyAutomaticReleases();
    assertThat(details.isHeld()).isTrue();
    assertThat(details.getHeldTimeMs()).isEqualTo(0);

    ShadowSystemClock.setUptimeMillis(31);
    details.applyAutomaticReleases();
    assertThat(details.isHeld()).isTrue();
    assertThat(details.getHeldTimeMs()).isEqualTo(30);

    ShadowSystemClock.setUptimeMillis(90);
    details.applyAutomaticReleases();
    assertThat(details.isHeld()).isFalse();
    assertThat(details.getHeldTimeMs()).isEqualTo(60);
  }

  @Test
  public void testAcquireFinalizeReferenceCounted() {
    ShadowSystemClock.setUptimeMillis(1);
    WakeLockDetails details = new WakeLockDetails(mWakeLock, "test", 0);

    details.acquire(-1);
    details.applyAutomaticReleases();
    assertThat(details.isHeld()).isTrue();
    assertThat(details.getHeldTimeMs()).isEqualTo(0);

    ShadowSystemClock.setUptimeMillis(31);
    details.applyAutomaticReleases();
    assertThat(details.isHeld()).isTrue();
    assertThat(details.getHeldTimeMs()).isEqualTo(30);

    ShadowSystemClock.setUptimeMillis(61);
    details.wakeLockReference.clear();
    details.applyAutomaticReleases();

    ShadowSystemClock.setUptimeMillis(90);
    details.applyAutomaticReleases();
    assertThat(details.isHeld()).isFalse();
    assertThat(details.getHeldTimeMs()).isEqualTo(60);
  }

  @Test
  public void testNotReferenceCounted() {
    ShadowSystemClock.setUptimeMillis(1);
    WakeLockDetails details =
        new WakeLockDetails(mWakeLock, "test", 0).setIsReferenceCounted(false);

    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();

    ShadowSystemClock.setUptimeMillis(11);
    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();

    ShadowSystemClock.setUptimeMillis(21);
    details.release();
    assertThat(details.isHeld()).isFalse();
    assertThat(details.getHeldTimeMs()).isEqualTo(20);
    assertThat(details.getLastReleaseTimeMs()).isEqualTo(21);

    ShadowSystemClock.setUptimeMillis(31);
    details.release();
    assertThat(details.isHeld()).isFalse();
    assertThat(details.getHeldTimeMs()).isEqualTo(20);
    assertThat(details.getLastReleaseTimeMs()).isEqualTo(21);
  }

  @Test
  public void testReferenceCounted() {
    WakeLockDetails details = new WakeLockDetails(mWakeLock, "test", 0);

    ShadowSystemClock.setUptimeMillis(1);
    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();

    ShadowSystemClock.setUptimeMillis(21);
    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();

    ShadowSystemClock.setUptimeMillis(41);
    details.release();
    assertThat(details.isHeld()).isTrue();

    ShadowSystemClock.setUptimeMillis(61);
    details.release();
    assertThat(details.isHeld()).isFalse();

    ShadowSystemClock.setUptimeMillis(81);
    assertThat(details.isHeld()).isFalse();
    assertThat(details.getLastReleaseTimeMs()).isEqualTo(61);
    assertThat(details.getHeldTimeMs()).isEqualTo(60);
  }
}
