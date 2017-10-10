---
id: api
title: API
---

Describing the terminology used in the library

## Metrics
`Metrics` classes are simple bags of values that can be added and subtracted with some convenience methods. They generally have public properties to make it easier to access and manipulate.

## Collectors
A `MetricCollector` exposes the current state of whatever it's measuring since the app was started: collectors maintain state in memory or rely on an underlying api/file provided by the operating system.

Collectors are most useful for taking snapshots of the current state of the system which can then be subtracted to estimate utilization between two points of time.

## Reporters
Reporters are simple wrapper classes that should be convenient to hook up with any analytics system to maintain consistent event labels.

## Serializers
Directly using the Java serializable API isn't a very good idea on Android, particularly on Dalvik (because it relies on reflection): these are a set of classes that can be useful to persist metrics to disk -- either for sharing between processes or simply persisting till upload.

## Composite{Metrics, Collector, Reporter, Serializer}
Wrapper classes that allow working on sets of objects to make it simple to set up and apply instrumentation.
