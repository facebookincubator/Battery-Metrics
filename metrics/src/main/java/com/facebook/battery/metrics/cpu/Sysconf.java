/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.metrics.cpu;

import android.annotation.SuppressLint;
import android.os.Build;
import android.system.Os;
import android.system.OsConstants;
import com.facebook.battery.metrics.core.SystemMetricsLogger;
import com.facebook.infer.annotation.Nullsafe;
import java.lang.reflect.InvocationTargetException;

/**
 * Use {@link libcore.io.Posix} to obtain values from SysConf without having to include call through
 * to JNI directly, and let the Android Framework's classes do that for us.
 *
 * <p>The singleton instance of the Posix class was made directly accessible from Lollipop (SDK 21)
 * onwards; from ICS to Lollipop we can access an instance at {@code libcore.io.Libcore.os} using
 * reflection.
 *
 * @see <a href="https://fburl.com/8o3hnt3k">The Posix Class in AOSP</a>
 * @see <a href="https://fburl.com/wf5sbpjs">The CPP implementation of Posix</a>
 * @see <a href="https://fburl.com/9kyylxzu">Libcore singleton with a Posix instance</a>
 */
/*package*/ @Nullsafe(Nullsafe.Mode.LOCAL)
class Sysconf {

  private static final String TAG = "Sysconf";

  @SuppressLint("ObsoleteSdkInt")
  public static long getScClkTck(long fallback) {
    long result = fallback;
    if (Build.VERSION.SDK_INT >= 21) {
      result = Os.sysconf(OsConstants._SC_CLK_TCK);
    } else {
      result = fromLibcore("_SC_CLK_TCK", fallback);
    }

    return result > 0 ? result : fallback;
  }

  @SuppressLint("ObsoleteSdkInt")
  public static long getScNProcessorsConf(long fallback) {
    if (Build.VERSION.SDK_INT >= 21) {
      return Os.sysconf(OsConstants._SC_NPROCESSORS_CONF);
    } else {
      return fromLibcore("_SC_NPROCESSORS_CONF", fallback);
    }
  }

  private static long fromLibcore(String field, long fallback) {
    try {
      Class osConstantsClass = Class.forName("libcore.io.OsConstants");
      int scClkTck = osConstantsClass.getField(field).getInt(null);
      Class libcoreClass = Class.forName("libcore.io.Libcore");
      Class osClass = Class.forName("libcore.io.Os");
      Object osInstance = libcoreClass.getField("os").get(null);
      // NULLSAFE_FIXME[Nullable Dereference]
      return (long) osClass.getMethod("sysconf", int.class).invoke(osInstance, scClkTck);
    } catch (NoSuchMethodException ex) {
      logReflectionException(ex);
    } catch (NoSuchFieldException ex) {
      logReflectionException(ex);
    } catch (IllegalAccessException ex) {
      logReflectionException(ex);
    } catch (InvocationTargetException ex) {
      logReflectionException(ex);
    } catch (ClassNotFoundException ex) {
      logReflectionException(ex);
    }

    return fallback;
  }

  private static void logReflectionException(Exception ex) {
    SystemMetricsLogger.wtf(TAG, "Unable to read _SC_CLK_TCK by reflection", ex);
  }
}
