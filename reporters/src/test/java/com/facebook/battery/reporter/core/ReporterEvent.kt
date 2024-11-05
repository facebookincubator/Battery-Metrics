/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.core

import com.facebook.battery.reporter.core.SystemMetricsReporter
import com.facebook.battery.reporter.core.SystemMetricsReporter.Event
import java.util.HashMap
import kotlin.jvm.JvmField

/**
 * This class implements SystemMetricsReporter.Event for testing purpose of all metrics reporters.
 */
class ReporterEvent : SystemMetricsReporter.Event {

  @JvmField var eventMap: HashMap<String, Any> = HashMap()

  override fun acquireEvent(moduleName: String?, eventName: String) = Unit

  override fun add(key: String, value: String) {
    eventMap[key] = value
  }

  override fun add(key: String, value: Int) {
    eventMap[key] = value
  }

  override fun add(key: String, value: Long) {
    eventMap[key] = value
  }

  override fun add(key: String, value: Double) {
    eventMap[key] = value
  }

  override fun isSampled(): Boolean = true

  override fun logAndRelease() = Unit
}
