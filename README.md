# Battery Metrics
Battery Metrics is a lightweight android library to quickly instrument several metrics for understanding battery consumption.

As a developer, it's surprisingly hard to understand how your application affects battery life on Android &mdash; relying on the operating system level reported battery level tends to be inaccurate (because the reported levels are fairly coarse, affected by every app running on the device and smoothed out) and while it's possible to get really good measurements locally you don't really know what sort of experience users are having in the wild.

The library helps instrument hardware utilization to be able to understand how the application is behaving -- most of the underlying hardware metrics are either exposed by the OS, or not directly accessible -- which is where this library comes into play. We've written several metrics collectors that read from procfiles, or provide a consistent way to call into to instrument your application.

## Alpha
This is an initial release coinciding with our tech talk at Mobile@Scale Boston to get feedback, tweak the API and make the library more useful; we'll firm up the code and keep tweaking it till a full release of the library. Make sure to check out our roadmap!

## Quick Start

As a simple quickstart, let's instrument sample activity to check CPU time while the activity is being used in the foreground --

```
class SampleActivity extends Activity {

  private static final CpuMetricsCollector sCollector = new CpuMetricsCollector();
  private final CpuMetrics mInitialMetrics = sCollector.createMetrics();
  private final CpuMetrics mFinalMetrics = sCollector.createMetrics();

  @Override
  protected void onResume() {
    super.onResume();
    sCollector.getSnapshot(mInitialMetrics);
  }

  @Override
  protected void onPause() {
    super.onPause();
    sCollector.getSnapshot(mFinalMetrics);

    Log.d("BatteryMetrics", mFinalMetrics.diff(mInitialMetrics).toString());
  }
}
```

And foregrounding and background the application prints the metrics to logcat --

```
CpuMetrics{userTimeS=0.06, systemTimeS=0.04, childUserTimeS=0.0, childSystemTimeS=0.0}
```

Building further on this, there are many more metrics to collect, and some utility classes to reduce boilerplate -- a more detailed deep dive into using the API is in the sample app: check out sample/../BatteryApplication.java; and a detailed reference in the javadocs at /docs/javadocs.

## Community
- Find us on (unsurprisingly) [[Facebook][https://www.facebook.com/groups/batterymetrics/?ref=bookmarks]].
- And [[Slack][https://batterymetrics.slack.com/]]

## License
BatteryMetrics is BSD-licensed. We also provide an additional patent grant.
