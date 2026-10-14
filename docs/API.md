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

Reporters follow a format of:
- Importing a relevant metrics class from com.facebook.battery.metrics
- Converting metrics class to JSON file format
- Importing a reporter class from com.facebook.battery.reporter
- Passing in an event object called from reporter class
- Updating event object with JSON data
- Handles JSON exceptions if necessary 

## Serializers
Directly using the Java serializable API isn't a very good idea on Android, particularly on Dalvik (because it relies on reflection): these are a set of classes that can be useful to persist metrics to disk -- either for sharing between processes or simply persisting till upload.

Serializers:
- Import relevant metrics class from com.facebook.battery.metrics
- Conduct serialization of metrics class data for future transmission
- Conduct deserialization of metrics class data to rebuild object from transmitted data

Serializers contain functions for:
- Serialization:
    - serializeContents takes in metrics class and reads data
    - Serialization process differs between classes and matches data format being transmitted through 
      metrics class data
    - Serializes class object into binary data
- Deserialization:
    - deserializeContents takes in Binary data and converts back to metrics class object
    - Deserialization process differs based on qualities f=required for each metrics class object
- Companion:
    - The companion object holds a version ID for each serializer class
- Helper:
    - Serializers may have helper functions to assist in its data processing

## Composite{Metrics, Collector, Reporter, Serializer}
Wrapper classes that allow working on sets of objects to make it simple to set up and apply instrumentation.
