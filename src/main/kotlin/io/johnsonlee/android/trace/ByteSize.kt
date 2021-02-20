package io.johnsonlee.android.trace

import java.util.Locale

internal const val KB = 1024L
internal const val MB = KB * 1024L
internal const val GB = MB * 1024L
internal const val TB = GB * 1024L

inline class ByteSize(val value: Long) {

    constructor(str: String) : this(fromHumanReadable(str))


    override fun toString(): String = when {
        value >= TB -> "${value / TB}TB"
        value >= GB -> "${value / GB}GB"
        value >= MB -> "${value / MB}MB"
        value >= KB -> "${value / KB}KB"
        else -> "${value}B"
    }

}

private fun fromHumanReadable(str: String): Long {
    val hrs = str.toUpperCase(Locale.getDefault())
    val b = hrs.indexOf('B').takeIf { it > 0 } ?: return 0
    val num: (Int) -> Long = {
        str.substring(0, it).takeIf(String::isNotEmpty)?.toLong() ?: 0
    }

    return when (hrs[b - 1]) {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> num(b)
        'K' -> num(b - 1) * KB
        'M' -> num(b - 1) * MB
        'G' -> num(b - 1) * GB
        else -> num(b - 1) * TB
    }
}