## Introduction

A parser for Java stack trace, Android trace.txt and tombstone file parsing, usually used for issue aggregation in APM system.

## Usage

### Parse Java stack trace

```kotlin
val trace = JavaStackTraceParser().parse(Log.getStackTrace(e))
val rootCause = trace.rootCause
```

### Parse trace file

```kotlin
val trace = TraceFile.from(path)
val rootCause = trace.rootCause
```

or

```kotlin
val trace = FileReader(file).use {
    TraceFileParser(it).parse()
}
val rootCause = trace.rootCause
```

### Parse Android tombstone file

```kotlin
val tombstone = TombstoneFile.from(path)
val rootCause = tombstone.rootCause
```

or

```kotlin
val tombstone = FileReader(file).use {
    TombstoneFileParser(it).parse()
}
val rootCause = tombstone.rootCause
```

### Gradle

```kotlin
implementation("io.johnsonlee:trace-parser:$trace_parser_version")
```
