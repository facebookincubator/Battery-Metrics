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
    mWakeLock = powerManager.newWakeLock(0, "test");
  }

  @Test
  public void testAcquireRelease() {
    ShadowSystemClock.setUptimeMillis(1);
    WakeLockDetails details = new WakeLockDetails(mWakeLock, "test", 0);

    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();
    assertThat(details.getHeldTimeMs()).isEqualTo(0);

    ShadowSystemClock.setUptimeMillis(10);
    details.release();
    assertThat(details.isHeld()).isFalse();
    assertThat(details.getHeldTimeMs()).isEqualTo(9);
  }

  @Test
  public void testAcquireTimeout() {
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
  public void testUnreferenceCounted() {
    WakeLockDetails details =
        new WakeLockDetails(mWakeLock, "test", 0).setIsReferenceCounted(false);

    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();

    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();

    details.release();
    assertThat(details.isHeld()).isFalse();
  }

  @Test
  public void testReferenceCounted() {
    WakeLockDetails details = new WakeLockDetails(mWakeLock, "test", 0);

    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();

    details.acquire(-1);
    assertThat(details.isHeld()).isTrue();

    details.release();
    assertThat(details.isHeld()).isTrue();

    details.release();
    assertThat(details.isHeld()).isFalse();
  }
}
