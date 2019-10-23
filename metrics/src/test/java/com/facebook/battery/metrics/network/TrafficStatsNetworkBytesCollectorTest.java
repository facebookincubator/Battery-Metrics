/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.network;

import static android.net.TrafficStats.UNSUPPORTED;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.MOBILE;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.RX;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.TX;
import static com.facebook.battery.metrics.network.NetworkBytesCollector.WIFI;
import static org.assertj.core.api.Java6Assertions.assertThat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetworkInfo;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowTrafficStats.class})
public class TrafficStatsNetworkBytesCollectorTest {

  private final long[] mBytes = new long[8];

  @Test
  public void testEmpty() throws Exception {
    TrafficStatsNetworkBytesCollector collector =
        new TrafficStatsNetworkBytesCollector(RuntimeEnvironment.application);
    collector.getTotalBytes(mBytes);
    assertThat(mBytes).isEqualTo(new long[8]);
  }

  @Test
  public void testInitialValues() throws Exception {
    ShadowTrafficStats.setUidRxBytes(10000);
    ShadowTrafficStats.setUidTxBytes(20000);

    TrafficStatsNetworkBytesCollector collector =
        new TrafficStatsNetworkBytesCollector(RuntimeEnvironment.application);
    assertThat(collector.getTotalBytes(mBytes)).isTrue();
    assertThat(mBytes).isEqualTo(new long[] {0, 0, 10000, 20000, 0, 0, 0, 0});
  }

  @Test
  public void testUnsupportedValues() throws Exception {
    ShadowTrafficStats.setUidRxBytes(UNSUPPORTED);
    ShadowTrafficStats.setUidTxBytes(UNSUPPORTED);
    TrafficStatsNetworkBytesCollector collector =
        new TrafficStatsNetworkBytesCollector(RuntimeEnvironment.application);
    assertThat(collector.getTotalBytes(mBytes)).isFalse();
  }

  @Test
  public void testBroadcastNetworkChanges() throws Exception {
    ShadowTrafficStats.setUidRxBytes(10000);
    ShadowTrafficStats.setUidTxBytes(20000);

    TrafficStatsNetworkBytesCollector collector =
        new TrafficStatsNetworkBytesCollector(RuntimeEnvironment.application);
    assertThat(collector.getTotalBytes(mBytes)).isTrue();

    ShadowTrafficStats.setUidRxBytes(11000);
    ShadowTrafficStats.setUidTxBytes(22000);

    ConnectivityManager connectivityManager =
        (ConnectivityManager)
            RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);
    ShadowConnectivityManager shadowConnectivityManager = Shadows.shadowOf(connectivityManager);
    NetworkInfo networkInfo =
        ShadowNetworkInfo.newInstance(null, ConnectivityManager.TYPE_WIFI, 0, true, true);
    shadowConnectivityManager.setActiveNetworkInfo(networkInfo);
    collector.mReceiver.onReceive(null, null);

    ShadowTrafficStats.setUidRxBytes(11100);
    ShadowTrafficStats.setUidTxBytes(22200);
    assertThat(collector.getTotalBytes(mBytes)).isTrue();

    assertThat(mBytes[RX | MOBILE]).isEqualTo(11000);
    assertThat(mBytes[TX | MOBILE]).isEqualTo(22000);
    assertThat(mBytes[RX | WIFI]).isEqualTo(100);
    assertThat(mBytes[TX | WIFI]).isEqualTo(200);
  }
}
