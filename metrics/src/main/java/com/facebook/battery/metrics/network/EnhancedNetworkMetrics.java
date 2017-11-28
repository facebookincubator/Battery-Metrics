// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.battery.metrics.network;

import android.support.annotation.Nullable;
import com.facebook.battery.metrics.core.SystemMetrics;

/** Alternative to {@link NetworkMetrics} which offers fg/bg app state distinction. */
public class EnhancedNetworkMetrics extends SystemMetrics<EnhancedNetworkMetrics> {

  /**
   * True if fg/bg distinction was possible; false otherwise and all data will have been assumed to
   * have happened in the foreground.
   */
  public boolean supportsBgDetection;

  public final NetworkMetrics fgMetrics = new NetworkMetrics();
  public final NetworkMetrics bgMetrics = new NetworkMetrics();

  public EnhancedNetworkMetrics() {}

  @Override
  public EnhancedNetworkMetrics sum(
      @Nullable EnhancedNetworkMetrics b, @Nullable EnhancedNetworkMetrics output) {
    if (output == null) {
      output = new EnhancedNetworkMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      fgMetrics.sum(b.fgMetrics, output.fgMetrics);
      bgMetrics.sum(b.bgMetrics, output.bgMetrics);
    }

    return output;
  }

  @Override
  public EnhancedNetworkMetrics diff(
      @Nullable EnhancedNetworkMetrics b, @Nullable EnhancedNetworkMetrics output) {
    if (output == null) {
      output = new EnhancedNetworkMetrics();
    }
    if (b == null) {
      output.set(this);
    } else {
      fgMetrics.diff(b.fgMetrics, output.fgMetrics);
      bgMetrics.diff(b.bgMetrics, output.bgMetrics);
    }

    return output;
  }

  @Override
  public EnhancedNetworkMetrics set(EnhancedNetworkMetrics b) {
    fgMetrics.set(b.fgMetrics);
    bgMetrics.set(b.bgMetrics);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EnhancedNetworkMetrics that = (EnhancedNetworkMetrics) o;

    if (supportsBgDetection != that.supportsBgDetection) return false;
    if (!fgMetrics.equals(that.fgMetrics)) return false;
    if (!bgMetrics.equals(that.bgMetrics)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = (supportsBgDetection ? 1 : 0);
    result = 31 * result + fgMetrics.hashCode();
    result = 31 * result + bgMetrics.hashCode();
    return result;
  }
}
