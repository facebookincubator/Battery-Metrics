// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import com.facebook.battery.metrics.core.SystemMetricsTest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class EnhancedNetworkMetricsTest extends SystemMetricsTest<EnhancedNetworkMetrics> {

  @Override
  protected Class<EnhancedNetworkMetrics> getClazz() {
    return EnhancedNetworkMetrics.class;
  }
}
