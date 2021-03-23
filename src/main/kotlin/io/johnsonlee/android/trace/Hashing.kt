package io.johnsonlee.android.trace

import java.security.MessageDigest

private const val HEX = "0123456789abcdef"

fun ByteArray.hexify(): String {
    val chars = CharArray(this.size * 2)
    for (i in this.indices) {
        val v = this[i].toInt() and 0xff
        chars[i * 2] = HEX[v ushr 4]
        chars[i * 2 + 1] = HEX[v and 0x0f]
    }
    return String(chars)
}

fun ByteArray.sha256(): String = MessageDigest.getInstance("SHA-256").digest(this).hexify()

fun String.sha256(): String = MessageDigest.getInstance("SHA-256").digest(this.toByteArray()).hexify()

fun String.md5(): String = MessageDigest.getInstance("MD5").digest(this.toByteArray()).hexify()
