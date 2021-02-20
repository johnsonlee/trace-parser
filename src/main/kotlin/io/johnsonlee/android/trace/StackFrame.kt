package io.johnsonlee.android.trace

abstract class StackFrame(private val snapshot: String) {

    abstract val isFromUser: Boolean

    override fun equals(other: Any?): Boolean {
        return other === this || (other is StackFrame && other.snapshot == snapshot)
    }

    override fun hashCode(): Int = snapshot.hashCode()

    override fun toString() = snapshot

}