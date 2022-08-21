package io.johnsonlee.android.trace

private const val INVALID_ADDRESS: Long = -1

private const val INVALID_MAP_NAME: String = ""

private const val INVALID_INDEX: Int = -1

private const val INVALID_FUNCTION_NAME: String = ""

private const val INVALID_FUNCTION_OFFSET: Int = -1

private const val PREFIX_DATA = "/data/"

private val systemLdPathPrefixes = setOf("/system/", "/apex/", "/vendor/", "/data/dalvik-cache/", "/data/app/com.android.chrome", "com.google.android.webview", "com.google.android.trichromelibrary")

class NativeStackFrame(snapshot: String) : StackFrame(snapshot) {

    private val pound: Int? by lazy {
        snapshot.indexOf('#').takeIf { it > -1 }
    }

    private val _pc: Int? by lazy {
        snapshot.indexOf(" pc ").takeIf { it > -1 }
    }

    private val sp3: Int? by lazy {
        _pc?.let { pc ->
            snapshot.indexOf(' ', pc + 4).takeIf { it > -1 }
        }
    }

    private val slash: Int? by lazy {
        sp3?.let { sp ->
            snapshot.indexOf('/', sp).takeIf { it > -1 }
        }
    }

    private val sp4: Int? by lazy {
        slash?.let { sp ->
            snapshot.indexOf(' ', sp).takeIf { it > -1 }
        }
    }

    private val rbrace: Int? by lazy {
        snapshot.lastIndexOf(')').takeIf { it > -1 }
    }

    private val lbrace: Int? by lazy {
        sp4?.let { sp ->
            snapshot.indexOf('(', sp).takeIf { it > -1 }
        }
    }

    private val plus: Int? by lazy {
        rbrace?.let { r ->
            snapshot.lastIndexOf('+', r).takeIf { it > -1 }
        }
    }

    private val map: Int? by lazy {
        if (isFromUser) {
            snapshot.lastIndexOf('/', lbrace ?: snapshot.length) + 1
        } else {
            slash
        }
    }

    override val isFromUser: Boolean by lazy {
        if (index <= 0) return@lazy false
        val slash = this.slash ?: return@lazy false
        snapshot.indexOf(PREFIX_DATA, slash) == slash || systemLdPathPrefixes.none {
            snapshot.indexOf(it, slash) == slash
        }
    }

    override val fingerprint: String by lazy {
        (map?.let(snapshot::substring) ?: snapshot).md5()
    }

    val index: Int by lazy {
        val pound = this.pound ?: return@lazy INVALID_INDEX
        val pc = _pc ?: return@lazy INVALID_INDEX
        snapshot.substring(pound + 1, pc).trim().toInt()
    }

    val pc: Long by lazy {
        val pc = _pc ?: return@lazy INVALID_ADDRESS
        val sp3 = this.sp3 ?: return@lazy INVALID_ADDRESS

        try {
            snapshot.substring(pc + 4, sp3).toLong(16)
        } catch (e: NumberFormatException) {
            INVALID_ADDRESS
        }
    }

    val mapName: String by lazy {
        map?.let {
            snapshot.substring(it, lbrace ?: snapshot.length).trim()
        } ?: INVALID_MAP_NAME
    }

    val functionName: String by lazy {
        val lbrace = this.lbrace ?: return@lazy INVALID_FUNCTION_NAME
        val plus = this.plus ?: return@lazy INVALID_FUNCTION_NAME
        snapshot.substring(lbrace + 1, plus)
    }

    val functionOffset: Int by lazy {
        val plus = this.plus ?: return@lazy INVALID_FUNCTION_OFFSET
        val rbrace = this.rbrace ?: return@lazy INVALID_FUNCTION_OFFSET
        snapshot.substring(plus + 1, rbrace).toInt()
    }

}

internal val REGEX_NATIVE_STACK_FRAME = Regex("\\s*native:\\s+#\\d+\\s+pc\\s+[\\da-fA-F]+\\s+.+")

internal val REGEX_BACKTRACE_FRAME = Regex("#\\d+\\s+pc\\s+[\\da-fA-F]+\\s+.+")

internal fun isNativeBacktraceElement(line: String): Boolean {
    return line matches REGEX_NATIVE_STACK_FRAME || line matches REGEX_BACKTRACE_FRAME
}