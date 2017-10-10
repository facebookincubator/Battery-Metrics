---
id: quickstart
title: Quick Start
---

Add a dependency to the battery metrics library -- we'll shortly be publishing to maven, but for now you can clone and install the library as a project.


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
