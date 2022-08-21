package io.johnsonlee.android.trace

fun identifyRootCause(stackTrace: String): StackFrame? = identifyRootCause(stackTrace.split("\n"))

fun identifyRootCause(stackTrace: List<String>): StackFrame? {
    val hasJavaStackFrame = stackTrace.any { it matches REGEX_JAVA_STACK_FRAME }
    val hasCausedBy = stackTrace.any { it matches REGEX_CAUSED_BY }
    val hasNativeStackFrame = stackTrace.any { it matches REGEX_NATIVE_STACK_FRAME }
    val hasBacktraceFrame = stackTrace.any { it matches REGEX_BACKTRACE_FRAME }

    return when {
        hasCausedBy -> {
            JavaStackTraceParser().parse(stackTrace).rootCause
        }
        hasBacktraceFrame -> {
            val native = stackTrace.filter(::isNativeBacktraceElement).map(::NativeStackFrame)
            native.rootCause ?: native.firstOrNull()
        }
        hasNativeStackFrame || hasJavaStackFrame -> {
            val java = stackTrace.filter(::isJavaStackTraceElement).map(::JavaStackFrame)
            val native = stackTrace.filter(::isNativeBacktraceElement).map(::NativeStackFrame)
            native.rootCause ?: java.rootCause ?: native.firstOrNull() ?: java.firstOrNull()
        }
        else -> {
            JavaStackTraceParser().parse(stackTrace).rootCause
        }
    }
}