package io.johnsonlee.android.trace

/**
 *
 * Plain java stack trace parser
 *
 * @author johnsonlee
 */
class JavaStackTraceParser {

    fun parse(stackTrace: String): List<JavaStackFrame> = parse(stackTrace.split("\n"))

    fun parse(stackTraces: Iterable<String>): List<JavaStackFrame> {
        val frames = mutableListOf<String>()
        val iterator = stackTraces.iterator()

        while (iterator.hasNext()) {
            val line = iterator.next()
            line.indexOf("at ").takeIf {
                it > -1
            }?.takeIf(line::isStackTraceElement)?.let {
                frames += line
            } ?: if (frames.isNotEmpty()) break
        }

        return frames.map(::JavaStackFrame)
    }


}

private fun String.isStackTraceElement(at: Int): Boolean {
    for (i in 0 until at) {
        if (!Character.isWhitespace(this[i])) {
            return false
        }
    }
    return true
}
