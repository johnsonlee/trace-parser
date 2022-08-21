package io.johnsonlee.android.trace

/**
 *
 * Plain java stack trace parser
 *
 * @author johnsonlee
 */
class JavaStackTraceParser {

    fun parse(stackTrace: String): List<JavaStackFrame> = parse(stackTrace.split("\n"))

    fun parse(stackTrace: List<String>): List<JavaStackFrame> {
        val frames = mutableListOf<String>()
        val iterator = stackTrace.findRootCause().iterator()

        while (iterator.hasNext()) {
            iterator.next().takeIf(::isJavaStackTraceElement)?.let(frames::add) ?: if (frames.isNotEmpty()) break
        }

        return frames.map(::JavaStackFrame)
    }

}

private fun List<String>.findRootCause(): List<String> {
    val lines = toMutableList()
    var last = lines.size
    for (i in lines.indices.reversed()) {
        if (isCausedBy(lines[i])) {
            val causedBy = lines.subList(i, last)
            if (causedBy.map(::JavaStackFrame).any(JavaStackFrame::isFromUser)) {
                return causedBy
            }
            last = i
        }
    }
    return this
}
