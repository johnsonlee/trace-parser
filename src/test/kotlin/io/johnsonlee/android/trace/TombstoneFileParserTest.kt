package io.johnsonlee.android.trace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExperimentalUnsignedTypes
class TombstoneFileParserTest {

    @Test
    fun `parse tombstone file`() {
        val tombstone = javaClass.getResourceAsStream("/tombstone.txt").use {
            TombstoneFileParser(it).parse()
        }
        assertNotNull(tombstone)
        assertEquals(7, tombstone.backtrace.size)
        val rootCause = tombstone.rootCause
        assertNotNull(rootCause)
        assertEquals(0, rootCause.index)
        assertEquals("/system/lib/libc.so", rootCause.mapName)
        assertEquals("pthread_mutex_lock", rootCause.functionName)
        assertEquals(1, rootCause.functionOffset)
    }

}