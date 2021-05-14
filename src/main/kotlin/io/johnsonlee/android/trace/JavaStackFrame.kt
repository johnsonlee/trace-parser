package io.johnsonlee.android.trace


private const val INVALID_FILE_NAME: String = ""

private const val INVALID_LINE_NUMBER: Int = -1

private const val INVALID_CLASS_NAME: String = ""

private const val INVALID_METHOD_NAME: String = ""

private val systemPackages = setOf("java.", "kotlin.", "android.", "androidx.", "com.android.", "dalvik.")

class JavaStackFrame(snapshot: String) : StackFrame(snapshot) {

    override val isFromUser: Boolean by lazy {
        systemPackages.none {
            className.startsWith(it)
        }
    }

    val className: String by lazy {
        val at = snapshot.indexOf("at ").takeIf { it > -1 } ?: return@lazy INVALID_CLASS_NAME
        val brace = snapshot.lastIndexOf('(').takeIf { it > at } ?: return@lazy INVALID_CLASS_NAME
        val dot = snapshot.lastIndexOf('.', brace).takeIf { it in (at + 1) until brace } ?: return@lazy INVALID_CLASS_NAME
        snapshot.substring(at + 3, dot)
    }

    val methodName: String by lazy {
        val at = snapshot.indexOf("at ").takeIf { it > -1 } ?: return@lazy INVALID_METHOD_NAME
        val brace = snapshot.lastIndexOf('(').takeIf { it > at } ?: return@lazy INVALID_METHOD_NAME
        val dot = snapshot.lastIndexOf('.', brace).takeIf { it in (at + 1) until brace } ?: return@lazy INVALID_METHOD_NAME
        snapshot.substring(dot + 1, brace)
    }

    val sourceFile: String by lazy {
        val lbrace = snapshot.indexOf('(')
        val rbrace = snapshot.indexOf(')')
        val colon = snapshot.lastIndexOf(':')

        if (lbrace <= -1 || rbrace <= -1) {
            INVALID_FILE_NAME
        } else if (colon <= -1) {
            snapshot.substring(lbrace + 1, rbrace)
        } else {
            snapshot.substring(lbrace + 1, colon)
        }
    }

    val lineNumber: Int by lazy {
        val brace = snapshot.lastIndexOf(')')
        val colon = snapshot.lastIndexOf(':')
        if (brace > colon && colon > -1) {
            try {
                snapshot.substring(colon + 1, brace).toInt()
            } catch (e: NumberFormatException) {
                INVALID_LINE_NUMBER
            }
        } else {
            INVALID_LINE_NUMBER
        }
    }

}