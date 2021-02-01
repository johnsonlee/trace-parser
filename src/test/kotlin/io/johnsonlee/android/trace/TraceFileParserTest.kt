package io.johnsonlee.android.trace

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TraceFileParserTest {

    @Test
    fun `parse trace file`() {
        javaClass.getResourceAsStream("/trace.txt").use {
            val trace = TraceFileParser(it).parse()
            val main = trace.threads.singleOrNull(ThreadInfo::isMainThread)
            assertNotNull(main)
            assertTrue(trace.threads.isNotEmpty())
        }
    }

}