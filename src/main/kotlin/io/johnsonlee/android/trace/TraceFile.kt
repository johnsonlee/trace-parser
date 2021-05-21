package io.johnsonlee.android.trace

import java.io.File
import java.util.Date

/**
 * Abstraction of `trace.txt` file
 *
 * @author johnsonlee
 */
class TraceFile(
        val pid: Long,
        val date: Date,
        val threads: List<ThreadInfo>
) {

    @ExperimentalUnsignedTypes
    companion object {

        fun from(file: File): TraceFile = file.reader().use {
            TraceFileParser(it).parse()
        }

    }

    val mainThreadInfo: ThreadInfo by lazy {
        threads.first(this::isMainThread)
    }

    val rootCause: StackFrame? by lazy {
        mainThreadInfo.stackTrace.rootCause
    }

    private fun isMainThread(thread: ThreadInfo): Boolean = pid == thread.sysTid.toLong()

}