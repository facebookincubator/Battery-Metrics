/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.sample;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.composite.CompositeMetricsCollector;
import com.facebook.battery.metrics.core.StatefulSystemMetricsCollector;
import com.facebook.battery.metrics.cpu.CpuFrequencyMetrics;
import com.facebook.battery.metrics.cpu.CpuFrequencyMetricsCollector;
import com.facebook.battery.metrics.cpu.CpuMetrics;
import com.facebook.battery.metrics.cpu.CpuMetricsCollector;
import com.facebook.battery.metrics.network.NetworkMetrics;
import com.facebook.battery.metrics.network.NetworkMetricsCollector;
import com.facebook.battery.metrics.time.TimeMetrics;
import com.facebook.battery.metrics.time.TimeMetricsCollector;
import com.facebook.battery.reporter.composite.CompositeMetricsReporter;
import com.facebook.battery.reporter.core.SystemMetricsReporter;
import com.facebook.battery.reporter.cpu.CpuFrequencyMetricsReporter;
import com.facebook.battery.reporter.cpu.CpuMetricsReporter;
import com.facebook.battery.reporter.network.NetworkMetricsReporter;
import com.facebook.battery.reporter.time.TimeMetricsReporter;
import com.facebook.battery.serializer.composite.CompositeMetricsSerializer;
import com.facebook.battery.serializer.cpu.CpuFrequencyMetricsSerializer;
import com.facebook.battery.serializer.cpu.CpuMetricsSerializer;
import com.facebook.battery.serializer.network.NetworkMetricsSerializer;
import com.facebook.battery.serializer.time.TimeMetricsSerializer;
import java.io.*;

/**
 * An application class that maintains a singleton instance of a system wide metrics collector and
 * demonstrates the battery metrics API as far as possible. Accordingly, this application class -
 * records cpu time, cpu frequencies, app uptime, and network transfer - creates an "event" every
 * time an activity is paused or resumed, as a very quick way of breaking down foreground/background
 * sessions - saves the last session's data to disk, which is loaded when the app is started (you
 * should really avoid IO on cold start -- I can get away with because this app is otherwise
 * pointless).
 *
 * <p>Interesting classes are commented with a "Note" -- you can simply look around for it.
 *
 * <p>If you're running the app live, you should run adb logcat -s "BatteryApplication:*" to see the
 * corresponding output.
 */
public class BatteryApplication extends Application
    implements Application.ActivityLifecycleCallbacks {
  private static final String LAST_SNAPSHOT = "lastsnapshot";

  private static volatile BatteryApplication sInstance;
  private final CompositeMetricsCollector mMetricsCollector;

  private final StatefulSystemMetricsCollector<CompositeMetrics, CompositeMetricsCollector>
      mStatefulCollector;
  private final CompositeMetricsReporter mMetricsReporter;
  private final CompositeMetricsSerializer mMetricsSerializer;
  private final SystemMetricsReporter.Event mEvent = new Event();

  public BatteryApplication() {
    super();

    // Note -- Creating a collector instance that's shared across the application can be fairly
    //         useful. You can set it up and hook up all the individual metrics collectors,
    //         tweaking them once.
    mMetricsCollector =
        new CompositeMetricsCollector.Builder()
            .addMetricsCollector(TimeMetrics.class, new TimeMetricsCollector())
            .addMetricsCollector(CpuFrequencyMetrics.class, new CpuFrequencyMetricsCollector())
            .addMetricsCollector(CpuMetrics.class, new CpuMetricsCollector())
            .addMetricsCollector(NetworkMetrics.class, new NetworkMetricsCollector(this))
            .build();

    // Note -- The Reporter and Serializer mimic the collector; they were mainly split out into
    //         separate modules to keep it simple to include only what you really needed.
    mMetricsReporter =
        new CompositeMetricsReporter()
            .addMetricsReporter(TimeMetrics.class, new TimeMetricsReporter())
            .addMetricsReporter(CpuMetrics.class, new CpuMetricsReporter())
            .addMetricsReporter(CpuFrequencyMetrics.class, new CpuFrequencyMetricsReporter())
            .addMetricsReporter(NetworkMetrics.class, new NetworkMetricsReporter());
    mMetricsSerializer =
        new CompositeMetricsSerializer()
            .addMetricsSerializer(TimeMetrics.class, new TimeMetricsSerializer())
            .addMetricsSerializer(CpuMetrics.class, new CpuMetricsSerializer())
            .addMetricsSerializer(CpuFrequencyMetrics.class, new CpuFrequencyMetricsSerializer())
            .addMetricsSerializer(NetworkMetrics.class, new NetworkMetricsSerializer());

    // Note -- The stateful collector is a useful abstraction that maintains state about when it
    //         was last triggered, making it simple to observe changes since the last call.
    //         It's a very simple piece of code to reduce boilerplate, you should check out the
    //         underlying source code.
    mStatefulCollector = new StatefulSystemMetricsCollector<>(mMetricsCollector);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    registerActivityLifecycleCallbacks(this);

    sInstance = this;

    try (DataInputStream input = new DataInputStream(new FileInputStream(openFile()))) {
      CompositeMetrics metrics = mMetricsCollector.createMetrics();

      // Note -- this reads in from the last serialized value
      mMetricsSerializer.deserialize(metrics, input);

      // Note -- We've been careful to have good, readable `toString` implementations for metrics
      Log.i("BatteryApplication", "Last saved snapshot:\n" + metrics.toString());

    } catch (IOException ioe) {
      Log.e("BatteryApplication", "Failed to deserialize", ioe);
    }
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

  @Override
  public void onActivityStarted(Activity activity) {}

  @Override
  public void onActivityResumed(Activity activity) {
    // Note: Triggering an update / difference on transition
    logMetrics("background");
  }

  @Override
  public void onActivityPaused(Activity activity) {
    // Note: Triggering an update on transition
    logMetrics("foreground");
  }

  @Override
  public void onActivityStopped(Activity activity) {}

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

  @Override
  public void onActivityDestroyed(Activity activity) {}

  private void logMetrics(String tag) {
    // Note -- this gets the difference from the last call / initialization of the StatefulCollector
    CompositeMetrics update = mStatefulCollector.getLatestDiffAndReset();

    // Check out the Event class in this folder: it should be able to wrap most analytics
    // implementations comfortably; this one simply logs everything to logcat.
    mEvent.acquireEvent(null, "BatteryMetrics");
    if (mEvent.isSampled()) {
      mEvent.add("dimension", tag);
      mMetricsReporter.reportTo(update, mEvent);
      mEvent.logAndRelease();
    }

    try (DataOutputStream output = new DataOutputStream(new FileOutputStream(openFile()))) {
      // Save data as required, as cheaply as possible.
      mMetricsSerializer.serialize(update, output);
    } catch (IOException ioe) {
      Log.e("BatteryApplication", "Failed to serialize", ioe);
    }
  }

  private File openFile() {
    return new File(getFilesDir(), LAST_SNAPSHOT);
  }
}
