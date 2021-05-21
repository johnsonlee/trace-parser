package io.johnsonlee.android.trace

import kotlin.test.Test
import kotlin.test.assertEquals

class NativeStackFrameTest {

    @Test
    fun `parse native backtrace 0`() {
        val frame = NativeStackFrame(" native: #00 pc 00000000003ccb5c  /system/lib64/libart.so (art::DumpNativeStack(std::__1::basic_ostream<char, std::__1::char_traits<char>>&, int, BacktraceMap*, char const*, art::ArtMethod*, void*)+208)")
        assertEquals(0, frame.index)
        assertEquals(0x00000000003ccb5c, frame.pc)
        assertEquals("/system/lib64/libart.so", frame.mapName)
        assertEquals("art::DumpNativeStack(std::__1::basic_ostream<char, std::__1::char_traits<char>>&, int, BacktraceMap*, char const*, art::ArtMethod*, void*)", frame.functionName)
        assertEquals(208, frame.functionOffset)
    }

    @Test
    fun `parse native backtrace 1`() {
        val frame = NativeStackFrame("  native: #01 pc 000000000049cdf4  /system/lib64/libart.so (art::Thread::DumpStack(std::__1::basic_ostream<char, std::__1::char_traits<char>>&, bool, BacktraceMap*, bool) const+348)")
        assertEquals(0x000000000049cdf4, frame.pc)
        assertEquals("/system/lib64/libart.so", frame.mapName)
        assertEquals("art::Thread::DumpStack(std::__1::basic_ostream<char, std::__1::char_traits<char>>&, bool, BacktraceMap*, bool) const", frame.functionName)
        assertEquals(348, frame.functionOffset)
    }

    @Test
    fun `parse native backtrace 8`() {
        val frame = NativeStackFrame("  native: #08 pc 000000000000255c  /data/app/io.johnsonlee.graffito-8HsPPY4tZ-aNtI2TLJBmeQ==/lib/arm64/libgraffito.so (???)")
        assertEquals(0x000000000000255c, frame.pc)
        assertEquals("lib/arm64/libgraffito.so", frame.mapName)
        assertEquals("", frame.functionName)
        assertEquals(-1, frame.functionOffset)
    }

    @Test
    fun `parse native backtrace without function`() {
        val frame = NativeStackFrame("  native: #08 pc 000000000000255c  /data/app/io.johnsonlee.graffito-8HsPPY4tZ-aNtI2TLJBmeQ==/lib/arm64/libgraffito.so")
        assertEquals(0x000000000000255c, frame.pc)
        assertEquals("lib/arm64/libgraffito.so", frame.mapName)
        assertEquals("", frame.functionName)
        assertEquals(-1, frame.functionOffset)
    }

}