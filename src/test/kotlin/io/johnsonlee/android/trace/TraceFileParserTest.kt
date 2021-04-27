package io.johnsonlee.android.trace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalUnsignedTypes
class TraceFileParserTest {

    @Test
    fun `parse trace file`() {
        val trace = javaClass.getResourceAsStream("/trace.txt").use {
            TraceFileParser(it).parse()
        }
        assertTrue(trace.threads.isNotEmpty())
        assertTrue(trace.threads.size == 16)
        assertNotNull(trace.mainThreadInfo)

        val rootCause = trace.rootCause as JavaStackFrame
        assertNotNull(rootCause)
        assertEquals("io.johnsonlee.graffito.MainActivity\$onCreate\$1", rootCause.className)
        assertEquals("onClick", rootCause.methodName)
        assertEquals("MainActivity.kt", rootCause.sourceFile)
        assertEquals(13, rootCause.lineNumber)

        val jit0 = trace.threads.first { thread ->
            thread.name == "Jit thread pool worker thread 0"
        }
        assertNotNull(jit0)
    }

}