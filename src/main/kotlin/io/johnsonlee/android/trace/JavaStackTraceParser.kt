package io.johnsonlee.android.trace

/**
 *
 * Plain java stack trace parser
 *
 * @author johnsonlee
 */
class JavaStackTraceParser {

    fun parse(stackTrace: String): List<JavaStackFrame> = parse(stackTrace.split("\n"))

    fun parse(stackTrace: Iterable<String>): List<JavaStackFrame> {
        val frames = mutableListOf<String>()
        val iterator = stackTrace.iterator()

        while (iterator.hasNext()) {
            iterator.next().takeIf(::isStackTraceElement)?.let(frames::add) ?: if (frames.isNotEmpty()) break
        }

        return frames.map(::JavaStackFrame)
    }


}

private fun isStackTraceElement(line: String): Boolean {
    val at = line.indexOf("at ").takeIf { it >= 0 } ?: return false
    for (i in 0 until at) {
        if (!Character.isWhitespace(line[i])) {
            return false
        }
    }
    return true
}
