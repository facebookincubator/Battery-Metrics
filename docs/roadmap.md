---
id: roadmap
title: Roadmap
---

# Roadmap

## Collecting feedback

We're currently collecting feedback and observing how useful everyone finds the battery metrics project, and will prioritize/structure our next steps based on the feedback received for the alpha version.

## Future releases (no particular order)
These are some tools we've found useful, and are planning to open source

### CPU Spin Detector
Periodically measures cpu utilization, logging a snapshot of active threads if the utilization is too high: this is very useful when aggregated across snapshots from several devices in production.

### Camera Leak Detector
- Maintain state about the application and currently open cameras (needs manual instrumentation to wrap the library)
- A simple application that just indicates if a camera is currently open or not on screen; this is a tiny developer tool to make sure that the camera was closed as expected.

### Client side power model
- A convenience class to estimate energy consumed using the android power profile that ships on the device.

### HealthStats integration
- Provide access to the [healthstats](https://developer.android.com/reference/android/os/health/HealthStats.html) API through the snapshot model.

## Extensions (we'd love contributions!)

We've been using BatteryMetrics internally and accordingly haven't prioritized integration with the rest of the Android open source ecosystem yet -- perhaps you could help us change that!

- Integration with common open source libraries, like OkHttp.
- Integration with analytics libraries, like firebase.
