package io.johnsonlee.android.trace

abstract class StackFrame(private val snapshot: String) {

    abstract val isFromUser: Boolean

    val signature: String by lazy(snapshot.trim()::md5)

    override fun equals(other: Any?): Boolean {
        return other === this || (other is StackFrame && other.signature == signature)
    }

    override fun hashCode(): Int = snapshot.hashCode()

    override fun toString(): String = snapshot

}