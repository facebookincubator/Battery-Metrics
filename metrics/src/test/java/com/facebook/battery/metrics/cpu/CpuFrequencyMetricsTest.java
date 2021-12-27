/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CpuFrequencyMetricsTest {

  @BeforeClass
  public static void overrideCores() {
    CpuFrequencyMetricsCollector.overrideCores();
  }

  @Test
  public void testDefaultValues() {
    CpuFrequencyMetrics metrics = new CpuFrequencyMetrics();
    assertThat(metrics.timeInStateS.length).isEqualTo(CpuFrequencyMetricsCollector.getTotalCores());
  }

  @Test
  public void testEquals() {
    CpuFrequencyMetrics metricsA = new CpuFrequencyMetrics();
    metricsA.timeInStateS[0].put(200, 2000);
    metricsA.timeInStateS[0].put(100, 1000);
    metricsA.timeInStateS[1].put(300, 3000);

    CpuFrequencyMetrics metricsB = new CpuFrequencyMetrics();
    metricsB.timeInStateS[0].put(100, 1000);
    metricsB.timeInStateS[0].put(200, 2000);
    metricsB.timeInStateS[1].put(300, 3000);

    assertThat(metricsA).isEqualTo(metricsB);
  }

  @Test
  public void testSum() {
    CpuFrequencyMetrics metricsA = new CpuFrequencyMetrics();
    metricsA.timeInStateS[0].put(100, 1);
    metricsA.timeInStateS[0].put(200, 2);

    metricsA.timeInStateS[1].put(1000, 1);

    CpuFrequencyMetrics metricsB = new CpuFrequencyMetrics();
    metricsB.timeInStateS[0].put(200, 5);
    metricsB.timeInStateS[0].put(300, 3);

    metricsB.timeInStateS[2].put(2000, 2);

    CpuFrequencyMetrics output = new CpuFrequencyMetrics();
    metricsA.sum(metricsB, output);

    assertThat(output.timeInStateS[0].size()).isEqualTo(3);
    assertThat(output.timeInStateS[0].get(100)).isEqualTo(1);
    assertThat(output.timeInStateS[0].get(200)).isEqualTo(7);
    assertThat(output.timeInStateS[0].get(300)).isEqualTo(3);

    assertThat(output.timeInStateS[1].size()).isEqualTo(1);
    assertThat(output.timeInStateS[1].get(1000)).isEqualTo(1);

    assertThat(output.timeInStateS[2].size()).isEqualTo(1);
    assertThat(output.timeInStateS[2].get(2000)).isEqualTo(2);

    assertThat(output.timeInStateS[3].size()).isEqualTo(0);
  }

  @Test
  public void testDiff() {
    CpuFrequencyMetrics metricsA = new CpuFrequencyMetrics();
    metricsA.timeInStateS[0].put(100, 100);
    metricsA.timeInStateS[0].put(200, 200);
    metricsA.timeInStateS[1].put(300, 300);
    metricsA.timeInStateS[2].put(400, 400);

    CpuFrequencyMetrics metricsB = new CpuFrequencyMetrics();
    metricsB.timeInStateS[0].put(100, 20);
    metricsB.timeInStateS[1].put(300, 40);

    CpuFrequencyMetrics output = new CpuFrequencyMetrics();
    metricsA.diff(metricsB, output);

    assertThat(output.timeInStateS[0].size()).isEqualTo(2);
    assertThat(output.timeInStateS[0].get(100)).isEqualTo(80);
    assertThat(output.timeInStateS[0].get(200)).isEqualTo(200);

    assertThat(output.timeInStateS[1].size()).isEqualTo(1);
    assertThat(output.timeInStateS[1].get(300)).isEqualTo(260);

    assertThat(output.timeInStateS[2].size()).isEqualTo(1);
    assertThat(output.timeInStateS[2].get(400)).isEqualTo(400);

    assertThat(output.timeInStateS[3].size()).isEqualTo(0);
  }

  @Test
  public void testDiffWithCoreReset() {
    CpuFrequencyMetrics metricsA = new CpuFrequencyMetrics();
    metricsA.timeInStateS[0].put(100, 100); // Core 0 - Normal core
    metricsA.timeInStateS[1].put(200, 50); // Core 1 - core reset and started again
    metricsA.timeInStateS[1].put(250, 100);
    // Core 3 - now offline

    CpuFrequencyMetrics metricsB = new CpuFrequencyMetrics();
    metricsB.timeInStateS[0].put(100, 50);
    metricsB.timeInStateS[1].put(200, 200);
    metricsB.timeInStateS[1].put(250, 75);
    metricsB.timeInStateS[2].put(300, 300);

    CpuFrequencyMetrics output = new CpuFrequencyMetrics();
    metricsA.diff(metricsB, output);

    assertThat(output.timeInStateS[0].size()).isEqualTo(1);
    assertThat(output.timeInStateS[0].get(100)).isEqualTo(50);

    assertThat(output.timeInStateS[1].size()).isEqualTo(2);
    assertThat(output.timeInStateS[1].get(200)).isEqualTo(50);
    assertThat(output.timeInStateS[1].get(250)).isEqualTo(100);

    assertThat(output.timeInStateS[2].size()).isEqualTo(0);
    assertThat(output.timeInStateS[3].size()).isEqualTo(0);
  }

  @Test
  public void testNullOutput() {
    CpuFrequencyMetrics metricsA = new CpuFrequencyMetrics();
    CpuFrequencyMetrics metricsB = new CpuFrequencyMetrics();
    assertThat(metricsA.diff(metricsB)).isNotNull();
  }

  @Test
  public void testNullSubtrahend() {
    CpuFrequencyMetrics metricsA = new CpuFrequencyMetrics();
    metricsA.timeInStateS[0].put(100, 200);
    metricsA.timeInStateS[1].put(200, 300);

    CpuFrequencyMetrics output = new CpuFrequencyMetrics();
    metricsA.diff(null, output);

    assertThat(metricsA).isEqualTo(output);
  }

  @Test
  public void testSingleCoreToJSONObject() throws Exception {
    CpuFrequencyMetrics metrics = new CpuFrequencyMetrics();
    metrics.timeInStateS[0].put(100, 100);

    JSONObject jsonObject = metrics.toJSONObject();

    assertThat(jsonObject).isNotNull();
    assertThat(jsonObject.length()).isEqualTo(1);
    assertThat(jsonObject.has("1")).isTrue();
    assertThat(jsonObject.get("1").toString()).isEqualTo("{\"100\":100}");
  }

  @Test
  public void testMultipleCoresToJSONObject() throws Exception {
    CpuFrequencyMetrics metrics = new CpuFrequencyMetrics();
    metrics.timeInStateS[0].put(100, 100);
    metrics.timeInStateS[2].put(200, 200);

    JSONObject jsonObject = metrics.toJSONObject();

    assertThat(jsonObject).isNotNull();
    assertThat(jsonObject.length()).isEqualTo(2);
    assertThat(jsonObject.has("1")).isTrue();
    assertThat(jsonObject.get("1").toString()).isEqualTo("{\"100\":100}");
    assertThat(jsonObject.has("4")).isTrue();
    assertThat(jsonObject.get("4").toString()).isEqualTo("{\"200\":200}");
  }

  @Test
  public void testCoreCombinationToJSONObject() throws Exception {
    CpuFrequencyMetrics metrics = new CpuFrequencyMetrics();
    metrics.timeInStateS[0].put(100, 100);
    metrics.timeInStateS[2].put(100, 100);
    metrics.timeInStateS[1].put(200, 200);
    metrics.timeInStateS[3].put(200, 200);

    JSONObject jsonObject = metrics.toJSONObject();

    assertThat(jsonObject).isNotNull();
    assertThat(jsonObject.length()).isEqualTo(2);
    assertThat(jsonObject.has("a")).isTrue();
    assertThat(jsonObject.get("a").toString()).isEqualTo("{\"200\":200}");
    assertThat(jsonObject.has("5")).isTrue();
    assertThat(jsonObject.get("5").toString()).isEqualTo("{\"100\":100}");
  }
}
