package io.johnsonlee.android.trace

import java.io.File

@ExperimentalUnsignedTypes
class TombstoneFile constructor(
        val fingerprint: String,
        val revision: Int,
        val abi: String,
        val pid: Long,
        val tid: Long,
        val threadName: String,
        val processName: String,
        val signal: Int,
        val code: Int,
        val faultAddress: ULong,
        val backtrace: List<NativeStackFrame>
) {

    companion object {

        fun from(file: File): TombstoneFile = file.reader().use {
            TombstoneFileParser(it).parse()
        }

    }

    val rootCause: NativeStackFrame? by lazy {
        backtrace.rootCause
    }

}