package io.johnsonlee.android.trace

import io.johnsonlee.LookAheadReader
import io.johnsonlee.LookAheadReader.Companion.EOF
import java.io.InputStream
import java.io.Reader
import java.io.StringReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

private val REGEX_DALVIK_THREADS = Regex("DALVIK THREADS \\((\\d+)\\):")

/**
 * Android trace file parser
 *
 * @author johnsonlee
 */
class TraceFileParser(source: Reader) {

    private val reader: LookAheadReader = LookAheadReader(source)

    constructor(source: InputStream) : this(source.bufferedReader())

    fun parse(): TraceFile {
        var pid = 0
        var date = 0L

        while (true) {
            val line = reader.readLine() ?: break
            if (line.startsWith("----- pid ") && line.indexOf(" at ", 10) != -1 && line.endsWith(" -----")) {
                val lar = LookAheadReader(StringReader(line.substring(10, line.length - 6)))
                // pid
                pid = lar.readUnsignedInt() ?: continue
                lar.skipWhitespace()

                // at
                val at = CharArray(2)
                lar.read(at)
                if (at[0] != 'a' || at[1] != 't') {
                    continue
                }
                lar.skipWhitespace()

                // date and time
                val s = lar.readLine() ?: continue
                try {
                    date = SimpleDateFormat("YYYY-MM-DD HH:mm:ss").parse(s).time
                } catch (e: ParseException) {
                    continue
                }
                break
            }
        }

        return TraceFile(pid, Date(date), parseThreads())
    }

    private fun parseThreads(): List<ThreadInfo> {
        var count = 0

        do {
            val line = reader.readLine() ?: break
            if (!line.startsWith("DALVIK THREADS (")) {
                continue
            }
            count = REGEX_DALVIK_THREADS.matchEntire(line)?.groupValues?.get(1)?.toInt() ?: 0
        } while (count == 0)

        return if (count <= 0) emptyList() else parseThreads(count)
    }


    private fun parseThreads(n: Int): List<ThreadInfo> {
        reader.skipBlankLines()

        val threads = mutableListOf<ThreadInfo>()
        repeat(n) {
            threads += parseDalvikThread() ?: return@repeat
        }

        reader.skipBlankLines()

        while (true) {
            threads += parseNativeThread() ?: break
        }

        return threads
    }

    private fun parseThreadAttributes(): Map<String, String> {
        val attrs = mutableMapOf<String, String>()

        lines@ while (true) {
            reader.skipWhitespace()
            when (reader.peak().toChar()) {
                '|' -> {
                    reader.read()
                    reader.skipWhitespace()
                    val line = reader.readLine()?.takeIf(String::isNotBlank) ?: break
                    val lar = LookAheadReader(StringReader(line), line.length)
                    attrs@ while (true) {
                        attrs += parseThreadAttribute(lar) ?: break@attrs
                    }
                }
                else -> break@lines
            }
        }

        return attrs
    }

    private fun parseThreadAttribute(reader: LookAheadReader): Pair<String, String>? {
        val key = parseThreadAttributeKey(reader) ?: return null
        val value = parseThreadAttributeValue(reader) ?: ""
        return key to value
    }

    private fun parseThreadAttributeKey(reader: LookAheadReader): String? {
        val s = StringBuilder()

        while (true) {
            val c = reader.read()
            if (EOF == c || c == 61 /* = */) {
                break
            }
            s.append(c.toChar())
        }

        return s.takeIf(StringBuilder::isNotEmpty)?.trim()?.toString()
    }

    private fun parseThreadAttributeValue(reader: LookAheadReader): String? {
        reader.skipWhitespace()
        return when (reader.peak()) {
            34 /* " */ -> reader.readDoubleQuotedString()
            39 /* ' */ -> reader.readSingleQuotedString()
            40 /* ( */ -> reader.readWrappedString(40, 41)?.trim()
            EOF -> null
            else -> {
                val s = StringBuilder()
                while (true) {
                    val c = reader.read()
                    if (EOF == c || Character.isWhitespace(c)) {
                        break
                    }
                    s.append(c.toChar())
                }
                s.takeIf(StringBuilder::isNotEmpty)?.toString()
            }
        }
    }

    private fun parseDalvikThread(): DalvikThreadInfo? {
        val name = reader.readDoubleQuotedString() ?: return null
        val daemon = parseThreadDaemon()
        val priority = parseThreadIntLabel("prio=")
        val tid = parseThreadIntLabel("tid=")
        val status = parseThreadStatus()
        val attrs = parseThreadAttributes()
        val (sched0, sched1) = attrs["sched"]?.split('/')?.map(String::toInt) ?: listOf(0, 0)
        val (schedstat0, schedstat1, schedstat2) = attrs["schedstat"]?.split(' ')?.map(String::toInt) ?: listOf(0, 0, 0)
        val (stack0, stack1) = attrs["stack"]?.split('-')?.map { it.substringAfter("0x").toLong(16) } ?: listOf(0L, 0L)
        val heldMutexes = attrs["held mutexes"]?.split(' ')?.mapNotNull {
            val lar = LookAheadReader(StringReader(it), it.length)
            val mutexName = lar.readDoubleQuotedString() ?: return@mapNotNull null
            val shared = lar.readLine()?.trim() == "(shared held)"
            MutexInfo(mutexName, shared)
        } ?: emptyList()

        return DalvikThreadInfo(
                name,
                daemon,
                priority,
                tid,
                status,
                attrs["group"] ?: "",
                attrs["sCount"]?.toInt() ?: 0,
                attrs["dsCount"]?.toInt() ?: 0,
                attrs["flags"]?.toInt() ?: 0,
                attrs["obj"]?.substringAfter("0x")?.toLong(16) ?: 0L,
                attrs["self"]?.substringAfter("0x")?.toLong(16) ?: 0L,
                attrs["sysTid"]?.toInt() ?: 0,
                attrs["nice"]?.toInt() ?: 0,
                attrs["cgrp"] ?: "default",
                sched0 to sched1,
                attrs["handle"]?.substringAfter("0x")?.toLong(16) ?: 0L,
                attrs["state"]?.firstOrNull() ?: '?',
                Triple(schedstat0, schedstat1, schedstat2),
                attrs["utm"]?.toInt() ?: 0,
                attrs["stm"]?.toInt() ?: 0,
                attrs["core"]?.toInt() ?: 0,
                attrs["HZ"]?.toInt() ?: 0,
                stack0 to stack1,
                attrs["stackSize"]?.toBytes() ?: 0,
                heldMutexes,
                parseStackFrames())
    }

    private fun parseNativeThread(): NativeThreadInfo? {
        val name = reader.readDoubleQuotedString() ?: return null
        val priority = parseThreadIntLabel("prio=")
        val status = reader.readLine()
        val attrs = parseThreadAttributes()
        val (schedstat0, schedstat1, schedstat2) = attrs["schedstat"]?.split(' ')?.map(String::toInt) ?: listOf(0, 0, 0)

        return NativeThreadInfo(
                name,
                priority,
                attrs["sysTid"]?.toInt() ?: 0,
                attrs["nice"]?.toInt() ?: 0,
                attrs["cgrp"] ?: "default",
                attrs["state"]?.firstOrNull() ?: '?',
                Triple(schedstat0, schedstat1, schedstat2),
                attrs["utm"]?.toInt() ?: 0,
                attrs["stm"]?.toInt() ?: 0,
                attrs["core"]?.toInt() ?: 0,
                attrs["HZ"]?.toInt() ?: 0,
                parseStackFrames()
        )
    }

    private fun parseStackFrames(): List<StackFrame> = readLinesWithoutBlank().mapNotNull {
        val line = it.trim()
        when {
            line.startsWith("kernel: ") -> KernelStackFrame(line)
            line.startsWith("native: ") -> NativeStackFrame(line)
            line.startsWith("at ") -> JavaStackFrame(line)
            else -> null
        }
    }


    private fun parseThreadDaemon(): Boolean {
        reader.skipWhitespace()

        val daemon = CharArray(6)
        val n = reader.read(daemon, 0, daemon.size)
        if ("daemon" == String(daemon)) {
            return true
        }
        reader.unread(daemon, 0, n)
        return false
    }


    private fun parseThreadIntLabel(prefix: String): Int {
        reader.skipWhitespace()

        val buf = CharArray(prefix.length)
        val n = reader.read(buf, 0, buf.size)
        if (n < buf.size || prefix != String(buf)) {
            reader.unread(buf, 0, n)
            return -1
        }
        return reader.readUnsignedInt() ?: 0
    }

    private fun parseThreadStatus(): ThreadStatus {
        reader.skipWhitespace()
        return ThreadStatus.valueOf(reader.readLine()!!.trim())
    }

    private fun readLinesWithoutBlank(): List<String> {
        val lines = mutableListOf<String>()
        while (true) {
            lines += reader.readLine()?.takeIf(String::isNotBlank) ?: break
        }
        return lines
    }

}

private fun String.toBytes(): Long {
    val hrs = toUpperCase()
    val b = hrs.indexOf("B").takeIf { it > 0 } ?: return 0
    val num: (Int) -> Long = {
        substring(0, it).takeIf(String::isNotEmpty)?.toLong() ?: 0
    }
    return when (hrs[b - 1]) {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> num(b)
        'K' -> num(b - 1) * KB
        'M' -> num(b - 1) * MB
        'G' -> num(b - 1) * GB
        else -> num(b - 1) * TB
    }
}

