package io.johnsonlee.android.trace

class KernelStackFrame(snapshot: String) : StackFrame(snapshot) {

    override val isFromUser: Boolean = false

}