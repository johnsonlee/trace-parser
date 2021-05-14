package io.johnsonlee.android.trace


private const val INVALID_FILE_NAME: String = ""

private const val INVALID_LINE_NUMBER: Int = -1

private const val INVALID_CLASS_NAME: String = ""

private const val INVALID_METHOD_NAME: String = ""

private val systemPackages = setOf(
        "java.",
        "kotlin.",
        "android.",
        "androidx.",
        "com.android.",
        "dalvik."
)

class JavaStackFrame(snapshot: String) : StackFrame(snapshot) {

    private val at: Int? by lazy {
        snapshot.indexOf("at ").takeIf { it > -1 }
    }

    private val rbrace: Int? by lazy {
        snapshot.lastIndexOf(')').takeIf { it > -1 }
    }

    private val colon: Int? by lazy {
        rbrace?.let { r ->
            snapshot.lastIndexOf(':', r).takeIf { it > -1 }
        }
    }

    private val lbrace: Int? by lazy {
        (colon ?: rbrace)?.let { r ->
            snapshot.lastIndexOf('(', r).takeIf { it > -1 }
        }
    }

    private val dot: Int? by lazy {
        lbrace?.let { l ->
            snapshot.lastIndexOf('.', l).takeIf { it > -1 }
        }
    }

    override val isFromUser: Boolean by lazy {
        systemPackages.none {
            className.startsWith(it)
        }
    }

    val className: String by lazy {
        val at = this.at ?: return@lazy INVALID_CLASS_NAME
        val dot = this.dot ?: return@lazy INVALID_CLASS_NAME
        val slash = snapshot.lastIndexOf('/', dot).takeIf { it > -1 }
        snapshot.substring(slash ?: (at + 3), dot)
    }

    val methodName: String by lazy {
        val lbrace = this.lbrace ?: return@lazy INVALID_METHOD_NAME
        val dot = this.dot ?: return@lazy INVALID_METHOD_NAME
        snapshot.substring(dot + 1, lbrace)
    }

    val sourceFile: String by lazy {
        val lbrace = this.lbrace ?: return@lazy INVALID_FILE_NAME
        val rbrace = this.rbrace ?: return@lazy INVALID_FILE_NAME
        snapshot.substring(lbrace + 1, this.colon ?: rbrace)
    }

    val lineNumber: Int by lazy {
        val rbrace = this.rbrace ?: return@lazy INVALID_LINE_NUMBER
        this.colon?.let {
            snapshot.substring(it + 1, rbrace).toInt()
        } ?: INVALID_LINE_NUMBER
    }

}

val Iterable<JavaStackFrame>.rootCause
    get() = find(JavaStackFrame::isFromUser) ?: firstOrNull()