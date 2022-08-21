package io.johnsonlee.android.trace

abstract class StackFrame(private val snapshot: String) {

    init {
        if ('\n' in snapshot) {
            throw IllegalArgumentException(snapshot)
        }
    }

    abstract val isFromUser: Boolean

    open val fingerprint: String by lazy(snapshot.trim()::md5)

    override fun equals(other: Any?): Boolean {
        return other === this || (other is StackFrame && other.fingerprint == fingerprint)
    }

    override fun hashCode(): Int = snapshot.hashCode()

    override fun toString(): String = snapshot

}

val <T : StackFrame> Iterable<T>.rootCause: T?
    get() = firstOrNull {
        it.isFromUser
    }
