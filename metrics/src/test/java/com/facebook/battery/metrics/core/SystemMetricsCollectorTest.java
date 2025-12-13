/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.core;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class SystemMetricsCollectorTest<
    S extends SystemMetrics, T extends SystemMetricsCollector<S>> {

  @Rule public final ExpectedException mExpectedException = ExpectedException.none();

  protected abstract Class<T> getClazz();
}
