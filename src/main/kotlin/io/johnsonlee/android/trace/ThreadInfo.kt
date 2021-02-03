package io.johnsonlee.android.trace

abstract class ThreadInfo(
        val name: String,
        val priority: Int,
        val stackTrace: List<StackFrame>
) {

    abstract val sysTid: Int
    abstract val nice: Int
    abstract val cgrp: String
    abstract val state: Char
    abstract val schedstat: Triple<Int, Int, Int>
    abstract val utm: Int
    abstract val stm: Int
    abstract val core: Int
    abstract val hz: Int
}

data class MutexInfo(val name: String, val shared: Boolean) {
    override fun toString(): String = "\"${name}\"(${if (shared) "shared held" else "exclusive held"})"
}

class DalvikThreadInfo(
        name: String,
        val daemon: Boolean,
        priority: Int,
        val tid: Int,
        val status: ThreadStatus,
        val group: String,
        val sCount: Int,
        val dsCount: Int,
        val flags: Int,
        val obj: Long,
        val self: Long,
        override val sysTid: Int,
        override val nice: Int,
        override val cgrp: String,
        val sched: Pair<Int, Int>,
        val handle: Long,
        override val state: Char,
        override val schedstat: Triple<Int, Int, Int>,
        override val utm: Int,
        override val stm: Int,
        override val core: Int,
        override val hz: Int,
        val stack: Pair<Long, Long>,
        val stackSize: Long,
        val heldMutexes: List<MutexInfo>,
        stackTrace: List<StackFrame>
) : ThreadInfo(name, priority, stackTrace) {

    override fun toString() = stackTrace.joinToString("\n", """
    |"$name"${if (daemon) " daemon" else ""} prio=${priority} tid=${tid} $status
    |  | group="$group" sCount=$sCount dsCount=$dsCount flags=$flags obj=0x${obj.toString(16)} self=0x${self.toString(16)}
    |  | sysTid=$sysTid nice=$nice cgrp=$cgrp sched=${sched.first}/${sched.second} handle=0x${handle.toString(16)}
    |  | state=$state schedstat=( ${schedstat.first} ${schedstat.second} ${schedstat.third} ) utm=$utm stm=$stm core=$core HZ=$hz
    |  | stack=0x${stack.first}-0x${stack.second} stackSize=${stackSize.prettySize()}
    |  | held mutexes=${heldMutexes.joinToString(" ", " ")}
    |
    """.trimMargin()) { "  $it" }
}

class NativeThreadInfo(
        name: String,
        priority: Int,
        override val sysTid: Int,
        override val nice: Int,
        override val cgrp: String,
        override val state: Char,
        override val schedstat: Triple<Int, Int, Int>,
        override val utm: Int,
        override val stm: Int,
        override val core: Int,
        override val hz: Int,
        stackTrace: List<StackFrame>
) : ThreadInfo(name, priority, stackTrace) {

    override fun toString() = stackTrace.joinToString("\n", """
    |"$name" prio=${priority} (not attached)
    |  | sysTid=$sysTid nice=$nice cgrp=$cgrp
    |  | state=$state schedstat=( ${schedstat.first} ${schedstat.second} ${schedstat.third} ) utm=$utm stm=$stm core=$core HZ=$hz
    |
    """.trimMargin()) { "  $it" }

}

private fun Long.prettySize(): String = when {
    this >= TB -> "${this / TB}TB"
    this >= GB -> "${this / GB}GB"
    this >= MB -> "${this / MB}MB"
    this >= KB -> "${this / KB}KB"
    else -> "${this}B"
}

