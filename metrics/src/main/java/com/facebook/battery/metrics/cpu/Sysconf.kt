/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu

import android.system.Os
import android.system.OsConstants

/**
 * Use [libcore.io.Posix] to obtain values from SysConf without having to include call through to
 * JNI directly, and let the Android Framework's classes do that for us.
 *
 * @see [The Posix Class in AOSP](https://fburl.com/8o3hnt3k)
 * @see [The CPP implementation of Posix](https://fburl.com/wf5sbpjs)
 * @see [Libcore singleton with a Posix instance](https://fburl.com/9kyylxzu)
 */
/*package*/

internal object Sysconf {

  @JvmStatic
  fun getScClkTck(fallback: Long): Long {
    val result = Os.sysconf(OsConstants._SC_CLK_TCK)
    return if (result > 0) result else fallback
  }

  @get:JvmStatic
  val scNProcessorsConf: Long
    get() = Os.sysconf(OsConstants._SC_NPROCESSORS_CONF)
}
