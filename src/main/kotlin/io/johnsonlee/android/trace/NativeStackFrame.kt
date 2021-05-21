package io.johnsonlee.android.trace

private const val INVALID_ADDRESS: Long = -1

private const val INVALID_MAP_NAME: String = ""

private const val INVALID_INDEX: Int = -1

private const val INVALID_FUNCTION_NAME: String = ""

private const val INVALID_FUNCTION_OFFSET: Int = -1

private const val PREFIX_DATA = "/data/"

private val systemLdPathPrefixes = setOf("/system/", "/apex/")

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

    private val _snapshot: String by lazy {
        val l = lbrace
        val r = rbrace
        if (l != null && r != null) {
            mapName + snapshot.substring(l, r + 1)
        } else {
            mapName
        }
    }

    override val isFromUser: Boolean by lazy {
        val slash = this.slash ?: return@lazy false
        snapshot.indexOf(PREFIX_DATA, slash) == slash || systemLdPathPrefixes.none {
            snapshot.indexOf(it, slash) == slash
        }
    }

    override val signature: String by lazy {
        _snapshot.md5()
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
        val slash = this.slash ?: return@lazy INVALID_MAP_NAME
        if (isFromUser) {
            val index = snapshot.indexOf("==/", slash).takeIf { it > -1 }?.let { it + 3 }
                    ?: snapshot.indexOf("=/", slash).takeIf { it > -1 }?.let { it + 2 }
                    ?: snapshot.indexOf("/lib/", slash).takeIf { it > -1 }?.let { it + 1 }
                    ?: slash
            snapshot.substring(index, lbrace ?: snapshot.length).trim()
        } else {
            fullMapName
        }
    }

    val fullMapName: String by lazy {
        val slash = this.slash ?: return@lazy INVALID_MAP_NAME
        val lbrace = this.lbrace ?: this.snapshot.length
        snapshot.substring(slash, lbrace).trim()
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