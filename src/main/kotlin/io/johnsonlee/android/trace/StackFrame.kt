package io.johnsonlee.android.trace

abstract class StackFrame(private val snapshot: String) {

    override fun toString() = snapshot

}