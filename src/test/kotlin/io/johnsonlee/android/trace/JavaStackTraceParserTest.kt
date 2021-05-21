package io.johnsonlee.android.trace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JavaStackTraceParserTest {

    @Test
    fun `parse java stack trace`() {
        val stackTrace = """
android.view.WindowManager${'$'}BadTokenException: Unable to add window -- token android.os.BinderProxy@e2815e is not valid; is your activity running?
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
        val rootCause = frames.rootCause
        assertNotNull(rootCause)
        assertEquals("ViewRootImpl.java", rootCause.sourceFile)
        assertEquals("setView", rootCause.methodName)
        assertEquals(679, rootCause.lineNumber)
    }

    @Test
    fun `parse android stack trace`() {
        val stackTrace = """
java.lang.RuntimeException: Fatal Crash
        at com.example.foo.CrashyClass.sendMessage(CrashyClass.java:10)
        at com.example.foo.CrashyClass.crash(CrashyClass.java:6)
        at com.bugsnag.android.example.ExampleActivity.crashUnhandled(ExampleActivity.kt:55)
        at com.bugsnag.android.example.ExampleActivity${'$'}onCreate${'$'}1.invoke(ExampleActivity.kt:33)
        at com.bugsnag.android.example.ExampleActivity${'$'}onCreate${'$'}1.invoke(ExampleActivity.kt:14)
        at com.bugsnag.android.example.ExampleActivity${'$'}sam${'$'}android_view_View_OnClickListener${'$'}0.onClick(ExampleActivity.kt)
        at android.view.View.performClick(View.java:5637)
        at android.view.View${'$'}PerformClick.run(View.java:22429)
        at android.os.Handler.handleCallback(Handler.java:751)
        at android.os.Handler.dispatchMessage(Handler.java:95)
        at android.os.Looper.loop(Looper.java:154)
        at android.app.ActivityThread.main(ActivityThread.java:6119)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.ZygoteInit${'$'}MethodAndArgsCaller.run(ZygoteInit.java:886)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:776)"""

        val frames = JavaStackTraceParser().parse(stackTrace)
        assertTrue(frames.isNotEmpty())
        assertEquals(15, frames.size)
        val rootCause = frames.rootCause
        assertNotNull(rootCause)
        assertEquals("CrashyClass.java", rootCause.sourceFile)
        assertEquals("sendMessage", rootCause.methodName)
        assertEquals(10, rootCause.lineNumber)
    }


    @Test
    fun `parse android stack trace with causes`() {
        val stackTrace = """
Fatal Exception: java.lang.RuntimeException: Unable to start activity ComponentInfo{com.labpixies.flood/com.labpixies.flood.GameFinishedActivity}: java.lang.IllegalStateException: Only fullscreen opaque activities can request orientation
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3303)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3411)
        at android.app.ActivityThread.-wrap12(ActivityThread.java)
        at android.app.ActivityThread${'$'}H.handleMessage(ActivityThread.java:1994)
        at android.os.Handler.dispatchMessage(Handler.java:108)
        at android.os.Looper.loop(Looper.java:166)
        at android.app.ActivityThread.main(ActivityThread.java:7529)
        at java.lang.reflect.Method.invoke(Method.java)
        at com.android.internal.os.Zygote${'$'}MethodAndArgsCaller.run(Zygote.java:245)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:921)
Caused by: java.lang.IllegalStateException: Only fullscreen opaque activities can request orientation
       at android.app.Activity.onCreate(Activity.java:1081)
       at com.labpixies.flood.GameFinishedActivity.onCreate(GameFinishedActivity.java:81)
       at android.app.Activity.performCreate(Activity.java:7383)
       at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1218)
       at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3256)
       at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3411)
       at android.app.ActivityThread.-wrap12(ActivityThread.java)
       at android.app.ActivityThread${'$'}H.handleMessage(ActivityThread.java:1994)
       at android.os.Handler.dispatchMessage(Handler.java:108)
       at android.os.Looper.loop(Looper.java:166)
       at android.app.ActivityThread.main(ActivityThread.java:7529)
       at java.lang.reflect.Method.invoke(Method.java)
       at com.android.internal.os.Zygote${'$'}MethodAndArgsCaller.run(Zygote.java:245)
       at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:921)"""

        val frames = JavaStackTraceParser().parse(stackTrace)
        assertTrue(frames.isNotEmpty())
        assertEquals(14, frames.size)
        val rootCause = frames.rootCause
        assertNotNull(rootCause)
        assertEquals("GameFinishedActivity.java", rootCause.sourceFile)
        assertEquals("onCreate", rootCause.methodName)
        assertEquals(81, rootCause.lineNumber)
    }

}