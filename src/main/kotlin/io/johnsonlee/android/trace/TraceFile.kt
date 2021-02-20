package io.johnsonlee.android.trace

import java.io.File
import java.util.Date

/**
 * Abstraction of `trace.txt` file
 *
 * @author johnsonlee
 */
class TraceFile(val pid: Int, val date: Date, val threads: List<ThreadInfo>) {

    companion object {

        fun from(file: File): TraceFile = file.reader().use {
            TraceFileParser(it).parse()
        }

    }

    val mainThreadInfo: ThreadInfo by lazy {
        threads.first(this::isMainThread)
    }

    val rootCause: StackFrame? by lazy {
        mainThreadInfo.stackTrace.firstOrNull(StackFrame::isFromUser) ?: mainThreadInfo.stackTrace.firstOrNull()
    }

    private fun isMainThread(thread: ThreadInfo): Boolean = pid == thread.sysTid

}