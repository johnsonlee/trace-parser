package io.johnsonlee.android.trace

import io.johnsonlee.LookAheadReader
import java.io.InputStream
import java.io.Reader

private val REGEX_DALVIK_THREADS = Regex("DALVIK THREADS \\((\\d+)\\):")

/**
 * Android trace file parser
 */
class TraceFileParser private constructor(source: Reader) {

    private val reader: LookAheadReader = LookAheadReader(source)

    constructor(source: InputStream) : this(source.bufferedReader())

    fun parse(): TraceFile = parseDalvikThreads()

    private fun parseDalvikThreads(): TraceFile {
        var count = 0

        do {
            val line = reader.readLine() ?: break
            if (!line.startsWith("DALVIK THREADS (")) {
                continue
            }
            count = REGEX_DALVIK_THREADS.matchEntire(line)?.groupValues?.get(1)?.toInt() ?: 0
        } while (count == 0)

        return TraceFile(if (count <= 0) emptyList() else parseThreadList(count))
    }


    private fun parseThreadList(n: Int): List<ThreadInfo> {
        reader.skipBlankLines()

        return (0 until n).map {
            parseThread()
        }
    }

    private fun parseThread() = ThreadInfo(
            reader.readDoubleQuotedString()!!,
            readDaemon(),
            readIntLabel("prio="),
            readIntLabel("tid="),
            readThreadState(),
            readStackFrames()
    )

    private fun readStackFrames(): List<StackFrame> = readLinesWithoutBlank().mapNotNull {
        val line = it.trim()
        when {
            line.startsWith("kernel: ") -> KernelStackFrame(line)
            line.startsWith("native: ") -> NativeStackFrame(line)
            line.startsWith("at ") -> JavaStackFrame(line)
            else -> null
        }
    }


    private fun readDaemon(): Boolean {
        reader.skipWhitespace()

        val daemon = CharArray(6)
        val n = reader.read(daemon, 0, daemon.size)
        if ("daemon" == String(daemon)) {
            return true
        }
        reader.unread(daemon, 0, n)
        return false
    }


    private fun readIntLabel(prefix: String): Int {
        reader.skipWhitespace()

        val buf = CharArray(prefix.length)
        val n = reader.read(buf, 0, buf.size)
        if (n < buf.size || prefix != String(buf)) {
            reader.unread(buf, 0, n)
            return -1
        }
        return reader.readUnsignedInt() ?: 0
    }

    private fun readThreadState(): ThreadState {
        reader.skipWhitespace()
        return ThreadState.valueOf(reader.readLine()!!.trim())
    }

    private fun readLinesWithoutBlank(): List<String> {
        val lines = mutableListOf<String>()
        while (true) {
            lines += reader.readLine()?.takeIf(String::isNotBlank) ?: break
        }
        return lines
    }

}

