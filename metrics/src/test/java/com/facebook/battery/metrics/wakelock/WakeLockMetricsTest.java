/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.wakelock;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class WakeLockMetricsTest {

  @Test
  public void testEquals() {
    assertThat(new WakeLockMetrics()).isEqualTo(new WakeLockMetrics());
    assertThat(createInitializedMetrics()).isEqualTo(createInitializedMetrics());
  }

  @Test
  public void testDefaultValues() {
    WakeLockMetrics metrics = new WakeLockMetrics();
    assertThat(metrics.isAttributionEnabled).isFalse();
    assertThat(metrics.tagTimeMs.isEmpty()).isTrue();
    assertThat(metrics.heldTimeMs).isEqualTo(0);
    assertThat(metrics.acquiredCount).isEqualTo(0);
  }

  @Test
  public void testSetWithoutAttribution() {
    WakeLockMetrics metrics = new WakeLockMetrics();
    metrics.heldTimeMs = 10l;
    metrics.acquiredCount = 31;
    WakeLockMetrics alternate = new WakeLockMetrics();
    alternate.set(metrics);
    assertThat(alternate).isEqualTo(metrics);
  }

  @Test
  public void testSetWithAttribution() {
    WakeLockMetrics metrics = new WakeLockMetrics(true);
    metrics.heldTimeMs = 10l;
    metrics.acquiredCount = 23;
    metrics.tagTimeMs.put("TestWakeLock", 10l);

    WakeLockMetrics alternate = new WakeLockMetrics(true);
    alternate.set(metrics);
    assertThat(alternate).isEqualTo(metrics);
  }

  @Test
  public void testSumWithAttribution() {
    WakeLockMetrics metricsA = new WakeLockMetrics(true);
    metricsA.heldTimeMs = 20l;
    metricsA.acquiredCount = 4;
    metricsA.tagTimeMs.put("TestWakeLock1", 10l);
    metricsA.tagTimeMs.put("TestWakeLock3", 10l);

    WakeLockMetrics metricsB = new WakeLockMetrics(true);
    metricsB.heldTimeMs = 20l;
    metricsB.acquiredCount = 5;
    metricsB.tagTimeMs.put("TestWakeLock2", 10l);
    metricsB.tagTimeMs.put("TestWakeLock3", 10l);

    WakeLockMetrics output = new WakeLockMetrics(true);
    output.tagTimeMs.put("Test", 1L);
    output.heldTimeMs = 100L;
    output.acquiredCount = 442;
    metricsA.sum(metricsB, output);

    WakeLockMetrics expectedOutput = new WakeLockMetrics(true);
    expectedOutput.heldTimeMs = 40l;
    expectedOutput.acquiredCount = 9;
    expectedOutput.tagTimeMs.put("TestWakeLock1", 10l);
    expectedOutput.tagTimeMs.put("TestWakeLock2", 10l);
    expectedOutput.tagTimeMs.put("TestWakeLock3", 20l);

    assertThat(output).isEqualTo(expectedOutput);
  }

  @Test
  public void testDiffWithAttribution() {
    WakeLockMetrics metricsA = new WakeLockMetrics(true);
    metricsA.heldTimeMs = 25l;
    metricsA.acquiredCount = 100;
    metricsA.tagTimeMs.put("TestWakeLock1", 15l);
    metricsA.tagTimeMs.put("TestWakeLock2", 10l);

    WakeLockMetrics metricsB = new WakeLockMetrics(true);
    metricsB.heldTimeMs = 20l;
    metricsB.acquiredCount = 90;
    metricsB.tagTimeMs.put("TestWakeLock1", 10l);
    metricsB.tagTimeMs.put("TestWakeLock2", 10l);

    WakeLockMetrics output = new WakeLockMetrics(true);
    output.tagTimeMs.put("Test", 1L);
    output.acquiredCount = 341;
    output.heldTimeMs = 100L;
    metricsA.diff(metricsB, output);

    WakeLockMetrics expectedOutput = new WakeLockMetrics(true);
    expectedOutput.acquiredCount = 10;
    expectedOutput.heldTimeMs = 5l;
    expectedOutput.tagTimeMs.put("TestWakeLock1", 5l);

    assertThat(output).isEqualTo(expectedOutput);
  }

  @Test
  public void testAttributionToJSONObject() throws JSONException {
    WakeLockMetrics metrics = new WakeLockMetrics();
    metrics.isAttributionEnabled = true;
    metrics.acquiredCount = 2;
    metrics.heldTimeMs = 400;
    metrics.tagTimeMs.put("Test", 100l);

    JSONObject attribution = metrics.attributionToJSONObject();
    assertThat(attribution.length()).isEqualTo(1);
    assertThat(attribution.has("Test")).isTrue();
    assertThat(attribution.getLong("Test")).isEqualTo(100);
  }

  private WakeLockMetrics createInitializedMetrics() {
    WakeLockMetrics metrics = new WakeLockMetrics(true);
    metrics.acquiredCount = 100;
    metrics.heldTimeMs = 1000;
    metrics.tagTimeMs.put("TestA", 500L);
    metrics.tagTimeMs.put("TestB", 750L);
    return metrics;
  }
}
