package io.johnsonlee.android.trace

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TraceFileParserTest {

    @Test
    fun `parse trace file`() {
        javaClass.getResourceAsStream("/trace.txt").use {
            val trace = TraceFileParser(it).parse()
            assertTrue(trace.threads.isNotEmpty())
            assertTrue(trace.threads.size == 16)

            val main = trace.threads.singleOrNull(trace::isMainThread)
            assertNotNull(main)

            val jit0 = trace.threads.first { thread ->
                thread.name == "Jit thread pool worker thread 0"
            }
            assertNotNull(jit0)
        }
    }

}