---
id:gettingstarted
title:Getting Started
---

A more detailed guide than the quick start: the best way to follow along is to clone the repo and install the sample app attached; you can also look at the source code for BatteryApplication.java to see the inline documentation which mirrors this page.


## Using metrics and the collectors

Creating a metrics collector is simply choosing which metrics you want to instrument and collecting all of them into a composite metrics collector. The ones in this example rely on underlying os exposed files or Android APIs; see the inline documentation for hooking up instrumentation to the other, manual collectors.

```
   mMetricsCollector =
        new CompositeMetricsCollector.Builder()
            .addMetricsCollector(TimeMetrics.class, new TimeMetricsCollector())
            .addMetricsCollector(CpuFrequencyMetrics.class, new CpuFrequencyMetricsCollector())
            .addMetricsCollector(CpuMetrics.class, new CpuMetricsCollector())
            .addMetricsCollector(NetworkMetrics.class, new NetworkMetricsCollector(this))
            .build();
```

The way to access information from a collector is to simply `getSnapshot()` on it, passing in a Metrics object to store values. The API was designed in this way to prevent having to constantly reallocate metrics objects and being able to simply reuse them.

```
  CompositeMetrics metrics = mMetricsCollector.createMetrics();
  mMetricsCollector.getSnapshot(metrics);
```

The snapshots represent the values of all metrics at a given point of time -- they're most useful when compared against values at a previous moment. So the most common pattern of use we observed was:
```
  CompositeMetrics first = mMetricsCollector.createMetrics();
  CompositeMetrics second = mMetricsCollector.createMetrics();

  // At app startup
  mMetricsCollector.getSnapshot(first);

  // When the app is backgrounded
  mMetricsCollector.getSnapshot(second);
```

and then logging the difference
```
  Log.d("BatteryMetrics", first.diff(second).toString())
```


Instead of maintaining our own copies of metrics objects, this can all be simplified using a `StatefulSystemMetricsCollector`:
```
  // At app startup
  mStatefulCollector = new StatefulSystemMetricsCollector<>(mMetricsCollector);

  // When the app is backgrounded
  CompositeMetrics metrics = mStatefulCollector.getLatestDiffAndReset();
  Log.d("BatteryMetrics", metrics.toString());
```


## Extension: Using the reporters

Logging to logcat isn't that useful for obtaining metrics from production -- to make it easier to log battery metrics to the library, we can a `CompositeMetricsReporter` that calls through to your analytics library with fixed field names.

Creating the reporter is fairly familiar
```
  mMetricsReporter =
    new CompositeMetricsReporter()
        .addMetricsReporter(TimeMetrics.class, new TimeMetricsReporter())
        .addMetricsReporter(CpuMetrics.class, new CpuMetricsReporter())
        .addMetricsReporter(CpuFrequencyMetrics.class, new CpuFrequencyMetricsReporter())
        .addMetricsReporter(NetworkMetrics.class, new NetworkMetricsReporter());
```

and using it is simple

```
   CompositeMetrics metrics = ...; // however you populated it
   mEvent.acquireEvent(null, "BatteryMetrics");
   if (mEvent.isSampled()) {
     mMetricsReporter.reportTo(metrics, mEvent);
     mEvent.logAndRelease();
   }
```

The `Event` is fairly interesting and a bit different:
```
public class Event implements SystemMetricsReporter.Event {
  @Override
  public boolean isSampled() {
    // Whether we should bother recording this
    return true;
  }

  @Override
  public void acquireEvent(@Nullable String moduleName, String eventName) {
    // in case your library recycles event objects
    Log.i("BatteryApplication", "New event: {");
  }

  @Override
  public void add(String key, String value) {
    // Add values to the underlying analytics library
    Log.i("BatteryApplication", key + ":" + value);
  }

  @Override
  public void add(String key, int value) {
    // Add values to the underlying analytics library
    Log.i("BatteryApplication", key + ":" + value);
  }

  @Override
  public void add(String key, double value) {
    // Add values to the underlying analytics library
    Log.i("BatteryApplication", key + ":" + value);
  }

  @Override
  public void logAndRelease() {
    // Actually trigger logging
    Log.i("BatteryApplication", "}");
  }
}
```


## Extension: Using the serializers

Occasionally you might want to persist metrics to disk as well, and the serializers make that much more convenient.

The now familiar mechanism:
```
 mMetricsSerializer =
        new CompositeMetricsSerializer()
            .addMetricsSerializer(TimeMetrics.class, new TimeMetricsSerializer())
            .addMetricsSerializer(CpuMetrics.class, new CpuMetricsSerializer())
            .addMetricsSerializer(CpuFrequencyMetrics.class, new CpuFrequencyMetricsSerializer())
            .addMetricsSerializer(NetworkMetrics.class, new NetworkMetricsSerializer())
```

with simple serialization to save a metrics object to disk...
```
  CompositeMetrics metrics = mMetricsCollector.createMetrics();
  mMetricsCollector.getSnapshot(metrics);
  try (DataOutputStream output = new DataOutputStream(new FileOutputStream(openFile()))) {
    // Save data as required, as cheaply as possible.
    mMetricsSerializer.serialize(metrics, output);
  } catch (IOException ioe) {
    Log.e("BatteryApplication", "Failed to serialize", ioe);
  }
```

... and deserialization to populate one from disk.
```
    try (DataInputStream input = new DataInputStream(new FileInputStream(openFile()))) {
      CompositeMetrics metrics = mMetricsCollector.createMetrics();
      mMetricsSerializer.deserialize(metrics, input);
      Log.i("BatteryApplication", "Last saved snapshot:\n" + metrics.toString());
    } catch (IOException ioe) {
      Log.e("BatteryApplication", "Failed to deserialize", ioe);
    }
```


And that's it!
