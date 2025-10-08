/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu;

import android.system.Os;
import android.system.OsConstants;
import com.facebook.infer.annotation.Nullsafe;

/**
 * Use {@link libcore.io.Posix} to obtain values from SysConf without having to include call through
 * to JNI directly, and let the Android Framework's classes do that for us.
 *
 * @see <a href="https://fburl.com/8o3hnt3k">The Posix Class in AOSP</a>
 * @see <a href="https://fburl.com/wf5sbpjs">The CPP implementation of Posix</a>
 * @see <a href="https://fburl.com/9kyylxzu">Libcore singleton with a Posix instance</a>
 */
/*package*/ @Nullsafe(Nullsafe.Mode.LOCAL)
class Sysconf {

  public static long getScClkTck(long fallback) {
    long result = Os.sysconf(OsConstants._SC_CLK_TCK);
    return result > 0 ? result : fallback;
  }

  public static long getScNProcessorsConf() {
    return Os.sysconf(OsConstants._SC_NPROCESSORS_CONF);
  }
}
