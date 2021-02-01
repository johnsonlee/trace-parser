package io.johnsonlee.android.trace

private const val INVALID_ADDRESS: Long = -1

private const val INVALID_MAP_NAME: String = ""

private const val INVALID_INDEX: Int = -1

private const val INVALID_FUNCTION_NAME: String = ""

private const val INVALID_FUNCTION_OFFSET: Int = -1

class NativeStackFrame(snapshot: String) : StackFrame(snapshot) {

    val index: Int by lazy {
        val pound = snapshot.indexOf('#').takeIf { it > -1 } ?: return@lazy INVALID_INDEX
        val sp = snapshot.indexOf(' ', pound).takeIf { it > -1 } ?: return@lazy INVALID_INDEX
        snapshot.substring(pound + 1, sp).toInt()
    }

    val pc: Long by lazy {
        val pc = snapshot.indexOf("pc ").takeIf { it > -1 } ?: return@lazy INVALID_ADDRESS
        val sp = snapshot.indexOf(' ', pc).takeIf { it > -1 } ?: return@lazy INVALID_ADDRESS
        val len = sp - pc - 3

        if (len == 8 || len == 16) {
            try {
                snapshot.substring(pc + 3, sp).toLong()
            } catch (e: NumberFormatException) {
                INVALID_ADDRESS
            }
        } else {
            INVALID_ADDRESS
        }
    }

    val mapName: String by lazy {
        val pc = snapshot.indexOf("pc ").takeIf { it > -1 } ?: return@lazy INVALID_MAP_NAME
        val root = snapshot.indexOf('/', pc).takeIf { it > -1 } ?: return@lazy INVALID_MAP_NAME
        val sp = snapshot.indexOf(' ', root).takeIf { it > -1 } ?: return@lazy INVALID_MAP_NAME
        snapshot.substring(root, sp)
    }

    val functionName: String by lazy {
        val lbrace = snapshot.lastIndexOf('(').takeIf { it > -1 } ?: return@lazy INVALID_FUNCTION_NAME
        val rbrace = snapshot.lastIndexOf(')').takeIf { it > -1 } ?: return@lazy INVALID_FUNCTION_NAME
        val plus = snapshot.lastIndexOf('+', rbrace).takeIf { it in (lbrace + 1) until rbrace } ?: return@lazy INVALID_FUNCTION_NAME
        snapshot.substring(lbrace + 1, plus)
    }

    val functionOffset: Int by lazy {
        val lbrace = snapshot.lastIndexOf('(').takeIf { it > -1 } ?: return@lazy INVALID_FUNCTION_OFFSET
        val rbrace = snapshot.lastIndexOf(')').takeIf { it > -1 } ?: return@lazy INVALID_FUNCTION_OFFSET
        val plus = snapshot.lastIndexOf('+', rbrace).takeIf { it in (lbrace + 1) until rbrace } ?: return@lazy INVALID_FUNCTION_OFFSET
        snapshot.substring(plus + 1, rbrace).toInt()
    }

}