package io.johnsonlee.android.trace

import io.johnsonlee.LookAheadReader
import java.io.InputStream
import java.io.Reader
import java.io.StringReader

private const val PREFIX_FINGERPRINT = "Build fingerprint: "
private const val PREFIX_REVISION = "Revision: "
private const val PREFIX_ABI = "ABI: "
private const val PREFIX_PID = "pid: "
private const val PREFIX_TID = "tid: "
private const val PREFIX_THREAD_NAME = "name: "
private const val PREFIX_PROCESS_NAME = ">>> "
private const val PREFIX_SIGNAL = "signal "
private const val PREFIX_CODE = "code "
private const val PREFIX_FAULT_ADDR = "fault addr 0x"
private const val PREFIX_BACKTRACE = "backtrace:"
private const val PREFIX_FRAME = "    #"

/**
 * Android tombstone file parser
 *
 * @author johnsonlee
 */
@ExperimentalUnsignedTypes
class TombstoneFileParser(source: Reader) {

    private val reader: LookAheadReader = LookAheadReader(source)

    constructor(source: InputStream) : this(source.bufferedReader())

    fun parse(): TombstoneFile {
        var fingerprint = ""
        var revision = Int.MIN_VALUE
        var abi = ""
        var pid = Long.MIN_VALUE
        var tid = Long.MIN_VALUE
        var threadName = ""
        var processName = ""
        var signal = Int.MIN_VALUE
        var code = Int.MIN_VALUE
        var faultAddress = ULong.MAX_VALUE
        val backtrace = mutableListOf<String>()

        loop@ while (true) {
            val line = reader.readLine() ?: break@loop

            when {
                line.startsWith(PREFIX_FINGERPRINT) -> {
                    fingerprint = LookAheadReader(StringReader(line.substringAfter(PREFIX_FINGERPRINT))).use {
                        it.readSingleQuotedString()
                    } ?: ""
                }
                line.startsWith(PREFIX_REVISION) -> {
                    revision = LookAheadReader(StringReader(line.substringAfter(PREFIX_REVISION))).use {
                        it.readSingleQuotedString()?.toInt()
                    } ?: Int.MIN_VALUE
                }
                line.startsWith(PREFIX_ABI) -> {
                    abi = LookAheadReader(StringReader(line.substringAfter(PREFIX_ABI))).use {
                        it.readSingleQuotedString()
                    } ?: ""
                }
                line.startsWith(PREFIX_PID) -> {
                    pid = line.indexOf(PREFIX_PID).takeIf { it > -1 }?.let { index ->
                        LookAheadReader(StringReader(line.substring(index + PREFIX_PID.length))).use {
                            it.readDigits()?.toLong()
                        }
                    } ?: Long.MIN_VALUE
                    tid = line.indexOf(PREFIX_TID).takeIf { it > -1 }?.let { index ->
                        LookAheadReader(StringReader(line.substring(index + PREFIX_TID.length))).use {
                            it.readDigits()?.toLong()
                        }
                    } ?: Long.MIN_VALUE
                    threadName = line.indexOf(PREFIX_THREAD_NAME).takeIf { it > -1 }?.let { index ->
                        LookAheadReader(StringReader(line.substring(index + PREFIX_THREAD_NAME.length))).use {
                            it.readToken(" \t\r\n\u000C>")
                        }
                    } ?: ""
                    processName = line.indexOf(PREFIX_PROCESS_NAME).takeIf { it > -1 }?.let { index ->
                        LookAheadReader(StringReader(line.substring(index + PREFIX_PROCESS_NAME.length))).use {
                            it.readToken(" \t\r\n\u000C<")
                        }
                    } ?: ""
                }
                line.startsWith(PREFIX_SIGNAL) -> {
                    signal = line.indexOf(PREFIX_SIGNAL).takeIf { it > -1 }?.let { index ->
                        LookAheadReader(StringReader(line.substring(index + PREFIX_SIGNAL.length))).use {
                            it.readSignedInt()
                        }
                    } ?: Int.MIN_VALUE
                    code = line.indexOf(PREFIX_CODE).takeIf { it > -1 }?.let { index ->
                        LookAheadReader(StringReader(line.substring(index + PREFIX_CODE.length))).use {
                            it.readSignedInt()
                        }
                    } ?: Int.MIN_VALUE
                    faultAddress = line.indexOf(PREFIX_FAULT_ADDR).takeIf { it > -1 }?.let { index ->
                        LookAheadReader(StringReader(line.substring(index + PREFIX_CODE.length))).use {
                            it.readDigits()?.toULong(16)
                        }
                    } ?: ULong.MAX_VALUE
                }
                line.startsWith(PREFIX_BACKTRACE) -> {
                    backtrace@ while (true) {
                        val frame = reader.readLine() ?: break@loop
                        when {
                            frame.isBlank() -> continue@backtrace
                            frame.startsWith(PREFIX_FRAME) -> backtrace += frame
                            else -> {
                                if (backtrace.isNotEmpty()) {
                                    break@loop
                                }
                            }
                        }
                    }
                }
            }
        }

        return TombstoneFile(
                fingerprint = fingerprint,
                revision = revision,
                abi = abi,
                pid = pid,
                tid = tid,
                threadName = threadName,
                processName = processName,
                signal = signal,
                code = code,
                faultAddress = faultAddress,
                backtrace = backtrace.map(::NativeStackFrame)
        )
    }

}