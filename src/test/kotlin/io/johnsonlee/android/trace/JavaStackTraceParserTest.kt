package io.johnsonlee.android.trace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JavaStackTraceParserTest {

    @Test
    fun `parse java stack trace`() {
        val stackTrace = """android.view.WindowManager${'$'}BadTokenException: Unable to add window -- token android.os.BinderProxy@e2815e is not valid; is your activity running?
    at android.view.ViewRootImpl.setView(ViewRootImpl.java:679)
    at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:342)
    at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:93)
    at android.widget.Toast${'$'}TN.handleShow(Toast.java:459)
    at android.widget.Toast${'$'}TN${'$'}2.handleMessage(Toast.java:342)
    at android.os.Handler.dispatchMessage(Handler.java:102)
    at android.os.Looper.loop(Looper.java:154)
    at android.app.ActivityThread.main(ActivityThread.java:6119)
    at java.lang.reflect.Method.invoke(Native Method)"""
        val frames = JavaStackTraceParser().parse(stackTrace)
        assertTrue(frames.isNotEmpty())
        assertEquals(9, frames.size)
        val rootCause = frames.find(JavaStackFrame::isFromUser) ?: frames.firstOrNull()
        assertNotNull(rootCause)
        assertEquals("ViewRootImpl.java", rootCause.sourceFile)
        assertEquals("setView", rootCause.methodName)
        assertEquals(679, rootCause.lineNumber)
    }

}