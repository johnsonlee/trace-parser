## Introduction

A parser for Android trace & tombstone file parsing, usually used for issue aggregation in APM system.

## Usage

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

