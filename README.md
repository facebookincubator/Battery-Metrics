# Battery Metrics
<p align="center">
  <img src="/logo/logo.png?raw=true" title="Battery Metrics Logo"/>
</p>

Battery Metrics is a lightweight android library to quickly instrument several metrics for understanding battery consumption.

As a developer, it's surprisingly hard to understand how your application affects battery life on Android &mdash; relying on the operating system level reported battery level tends to be inaccurate (because the reported levels are fairly coarse, affected by every app running on the device and smoothed out) and while it's possible to get really good measurements locally you don't really know what sort of experience users are having in the wild.

The library helps instrument hardware utilization to be able to understand how the application is behaving -- most of the underlying hardware metrics are either exposed by the OS, or not directly accessible -- which is where this library comes into play. We've written several metrics collectors that read from procfiles, or provide a consistent way to call into to instrument your application.

## Adding BatteryMetrics

Add `jcenter` to your repositories –

```groovy
repositories {
  jcenter()
}
```

And add dependencies on the projects you'd like to use in `build.gradle` –

```groovy
dependencies {
    implementation 'com.facebook.battery:metrics:1.0.0'
    implementation 'com.facebook.battery:reporters:1.0.0'   // optional
    implementation 'com.facebook.battery:serializers:1.0.0' // optional
}
```

## Quick Start

As a simple quickstart, let's instrument sample activity to check CPU time while the activity is being used in the foreground --

```java
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

```java
CpuMetrics{userTimeS=0.06, systemTimeS=0.04, childUserTimeS=0.0, childSystemTimeS=0.0}
```


## Sample App

Building further on this, there are many more metrics to collect, and some utility classes to reduce boilerplate -- a more detailed deep dive into using the API is in the sample app: check out [sample/../BatteryApplication.java](https://github.com/facebookincubator/Battery-Metrics/blob/master/sample/src/main/java/com/facebook/battery/sample/BatteryApplication.java).

You can quickly install and run the app --
```
./gradlew :sample:installDebug
```

## Documentation
- [JavaDocs](https://facebookincubator.github.io/Battery-Metrics/) -- a reasonably comprehensive reference to all the exposed APIs.
- [Roadmap](https://github.com/facebookincubator/Battery-Metrics/blob/master/docs/roadmap.md) -- our planned, but unprioritized roadmap. Give us feedback!
- [Additional reading](https://github.com/facebookincubator/Battery-Metrics/blob/master/docs/references.md) -- some sources we've found useful.
- [API description](https://github.com/facebookincubator/Battery-Metrics/blob/master/docs/API.md) -- a brief description of the terms used throughout the project.
- [Getting started](https://github.com/facebookincubator/Battery-Metrics/blob/master/docs/gettingstarted.md) -- the core of the sample app described. Make sure you check out [sample/../BatteryApplication.java](https://github.com/facebookincubator/Battery-Metrics/blob/master/sample/src/main/java/com/facebook/battery/sample/BatteryApplication.java).
- [Mistrusting battery level](https://github.com/facebookincubator/Battery-Metrics/blob/master/docs/mistrustbatterylevel.md) -- why we don't rely on the os reported battery level
- [Contribution guidelines](https://github.com/facebookincubator/Battery-Metrics/blob/master/CONTRIBUTING.md) -- we'd love to see contributions to the project.

## Community
- Find us on (unsurprisingly) [Facebook](https://www.facebook.com/groups/batterymetrics/?ref=bookmarks).
- And [Slack](https://batterymetrics.slack.com/) -- and the [invite](https://goo.gl/Rb3kty).

## License
BatteryMetrics is MIT-licensed.
