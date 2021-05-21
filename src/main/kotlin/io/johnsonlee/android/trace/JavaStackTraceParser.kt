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
            iterator.next().takeIf(::isStackTraceElement)?.let(frames::add) ?: if (frames.isNotEmpty()) break
        }

        return frames.map(::JavaStackFrame)
    }


}

private fun List<String>.findRootCause(): Iterable<String> {
    val iterator = listIterator(size)
    while (iterator.hasPrevious()) {
        if (isCause(iterator.previous())) {
            return object : Iterable<String> {
                override fun iterator(): Iterator<String> = iterator
            }
        }
    }
    return this
}

private fun isCause(line: String): Boolean {
    val causedBy = line.indexOf("Caused by: ").takeIf { it >= 0 } ?: return false
    for (i in 0 until causedBy) {
        if (!Character.isWhitespace(line[i])) {
            return false
        }
    }
    return true
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
