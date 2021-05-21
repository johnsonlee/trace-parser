package io.johnsonlee.android.trace

import java.lang.IllegalArgumentException

abstract class StackFrame(protected val snapshot: String) {

    init {
        if ('\n' in snapshot) {
            throw IllegalArgumentException(snapshot)
        }
    }

    abstract val isFromUser: Boolean

    open val signature: String by lazy(snapshot.trim()::md5)

    override fun equals(other: Any?): Boolean {
        return other === this || (other is StackFrame && other.signature == signature)
    }

    override fun hashCode(): Int = snapshot.hashCode()

    override fun toString(): String = snapshot

}