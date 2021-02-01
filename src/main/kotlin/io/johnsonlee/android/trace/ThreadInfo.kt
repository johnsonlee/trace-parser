package io.johnsonlee.android.trace

data class ThreadInfo(
        val name: String,
        val daemon: Boolean,
        val priority: Int,
        val tid: Int,
        val state: ThreadState,
        val stackTrace: List<StackFrame>
) {

    fun isMainThread(): Boolean = name == "main" && tid == 1

    override fun toString() = stackTrace.joinToString("\n", "$name daemon=$daemon prio=${priority} tid=${tid} $state\n")
}