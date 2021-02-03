package io.johnsonlee.android.trace

import java.util.Date

/**
 * Abstraction of `trace.txt` file
 *
 * @author johnsonlee
 */
class TraceFile(val pid: Int, val date: Date, val threads: List<ThreadInfo>) {

    fun isMainThread(thread: ThreadInfo): Boolean = pid == thread.sysTid

}