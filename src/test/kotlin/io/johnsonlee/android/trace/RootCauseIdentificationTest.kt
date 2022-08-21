package io.johnsonlee.android.trace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RootCauseIdentificationTest {

    @Test
    fun `identify root cause of ANR with java stack frames`() {
        val stackTrace = """
        "main" prio=5 tid=1 Sleeping
          | group="main" sCount=1 dsCount=0 flags=1 obj=0x73e015f0 self=0x6fdacbea00
          | sysTid=27873 nice=-10 cgrp=default sched=0/0 handle=0x705f7d49a8
          | state=S schedstat=( 485151184 25248967 249 ) utm=40 stm=7 core=3 HZ=100
          | stack=0x7ffc56a000-0x7ffc56c000 stackSize=8MB
          | held mutexes=
          at java.lang.Thread.sleep(Native method)
          - sleeping on <0x048bde26> (a java.lang.Object)
          at java.lang.Thread.sleep(Thread.java:373)
          - locked <0x048bde26> (a java.lang.Object)
          at java.lang.Thread.sleep(Thread.java:314)
          at io.johnsonlee.graffito.MainActivity${'$'}onCreate${'$'}1.onClick(MainActivity.kt:13)
          at android.view.View.performClick(View.java:6294)
          at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:992)
          at android.view.View${'$'}PerformClick.run(View.java:24770)
          at android.os.Handler.handleCallback(Handler.java:790)
          at android.os.Handler.dispatchMessage(Handler.java:99)
          at android.os.Looper.loop(Looper.java:164)
          at android.app.ActivityThread.main(ActivityThread.java:6494)
          at java.lang.reflect.Method.invoke(Native method)
          at com.android.internal.os.RuntimeInit${'$'}MethodAndArgsCaller.run(RuntimeInit.java:438)
          at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:807)
        """.trimIndent()
        val rootCause = identifyRootCause(stackTrace)
        assertNotNull(rootCause)
        assertTrue(rootCause is JavaStackFrame)
        assertEquals("MainActivity.kt", rootCause.sourceFile)
        assertEquals("onClick", rootCause.methodName)
    }

    @Test
    fun `identify root cause of ANR with java stack frames and system native stack frames`() {
        val stackTrace = """
        "main" prio=5 tid=1 Sleeping
          | group="main" sCount=1 dsCount=0 flags=1 obj=0x73e015f0 self=0x6fdacbea00
          | sysTid=27873 nice=-10 cgrp=default sched=0/0 handle=0x705f7d49a8
          | state=S schedstat=( 485151184 25248967 249 ) utm=40 stm=7 core=3 HZ=100
          | stack=0x7ffc56a000-0x7ffc56c000 stackSize=8MB
          | held mutexes=
          native: #00 pc 0004793e  /system/lib/libc.so (pthread_mutex_lock+1)
          native: #01 pc 0001aa1b  /system/lib/libc.so (readdir+10)
          native: #02 pc 00001b91  /system/xbin/crasher (readdir_null+20)
          native: #03 pc 0000184b  /system/xbin/crasher (do_action+978)
          native: #04 pc 00001459  /system/xbin/crasher (thread_callback+24)
          native: #05 pc 00047317  /system/lib/libc.so (_ZL15__pthread_startPv+22)
          native: #06 pc 0001a7e5  /system/lib/libc.so (__start_thread+34)
          at java.lang.Thread.sleep(Native method)
          - sleeping on <0x048bde26> (a java.lang.Object)
          at java.lang.Thread.sleep(Thread.java:373)
          - locked <0x048bde26> (a java.lang.Object)
          at java.lang.Thread.sleep(Thread.java:314)
          at io.johnsonlee.graffito.MainActivity${'$'}onCreate${'$'}1.onClick(MainActivity.kt:13)
          at android.view.View.performClick(View.java:6294)
          at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:992)
          at android.view.View${'$'}PerformClick.run(View.java:24770)
          at android.os.Handler.handleCallback(Handler.java:790)
          at android.os.Handler.dispatchMessage(Handler.java:99)
          at android.os.Looper.loop(Looper.java:164)
          at android.app.ActivityThread.main(ActivityThread.java:6494)
          at java.lang.reflect.Method.invoke(Native method)
          at com.android.internal.os.RuntimeInit${'$'}MethodAndArgsCaller.run(RuntimeInit.java:438)
          at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:807)
        """.trimIndent()
        val rootCause = identifyRootCause(stackTrace)
        assertNotNull(rootCause)
        assertTrue(rootCause is JavaStackFrame)
        assertEquals("MainActivity.kt", rootCause.sourceFile)
        assertEquals("onClick", rootCause.methodName)
    }

    @Test
    fun `identify root cause of ANR with java stack frames and user native stack frames`() {
        val stackTrace = """
        "main" prio=5 tid=1 Sleeping
          | group="main" sCount=1 dsCount=0 flags=1 obj=0x73e015f0 self=0x6fdacbea00
          | sysTid=27873 nice=-10 cgrp=default sched=0/0 handle=0x705f7d49a8
          | state=S schedstat=( 485151184 25248967 249 ) utm=40 stm=7 core=3 HZ=100
          | stack=0x7ffc56a000-0x7ffc56c000 stackSize=8MB
          | held mutexes=
          native: #00 pc 0004793e  /system/lib/libc.so (pthread_mutex_lock+1)
          native: #01 pc 0001aa1b  /system/lib/libc.so (readdir+10)
          native: #02 pc 00001b91  /data/app/com.example.app-SfGgHWrZgkDTZ437L1I_cQ==/lib/arm/libgraffito.so
          native: #05 pc 00047317  /system/lib/libc.so (_ZL15__pthread_startPv+22)
          native: #06 pc 0001a7e5  /system/lib/libc.so (__start_thread+34)
          at java.lang.Thread.sleep(Native method)
          - sleeping on <0x048bde26> (a java.lang.Object)
          at java.lang.Thread.sleep(Thread.java:373)
          - locked <0x048bde26> (a java.lang.Object)
          at java.lang.Thread.sleep(Thread.java:314)
          at io.johnsonlee.graffito.MainActivity${'$'}onCreate${'$'}1.onClick(MainActivity.kt:13)
          at android.view.View.performClick(View.java:6294)
          at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:992)
          at android.view.View${'$'}PerformClick.run(View.java:24770)
          at android.os.Handler.handleCallback(Handler.java:790)
          at android.os.Handler.dispatchMessage(Handler.java:99)
          at android.os.Looper.loop(Looper.java:164)
          at android.app.ActivityThread.main(ActivityThread.java:6494)
          at java.lang.reflect.Method.invoke(Native method)
          at com.android.internal.os.RuntimeInit${'$'}MethodAndArgsCaller.run(RuntimeInit.java:438)
          at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:807)
        """.trimIndent()
        val rootCause = identifyRootCause(stackTrace)
        assertNotNull(rootCause)
        assertTrue(rootCause is NativeStackFrame)
        assertEquals("libgraffito.so", rootCause.mapName)
    }

    @Test
    fun `identify root cause of native crash`() {
        val stackTrace = """
        #00 pc 0004793e  /system/lib/libc.so (pthread_mutex_lock+1)
        #01 pc 0001aa1b  /system/lib/libc.so (readdir+10)
        #02 pc 00001b91  /system/xbin/crasher (readdir_null+20)
        #03 pc 0000184b  /system/xbin/crasher (do_action+978)
        #04 pc 00001459  /system/xbin/crasher (thread_callback+24)
        #05 pc 00047317  /system/lib/libc.so (_ZL15__pthread_startPv+22)
        #06 pc 0001a7e5  /system/lib/libc.so (__start_thread+34)
        """.trimIndent()
        val rootCause = identifyRootCause(stackTrace)
        assertNotNull(rootCause)
        assertTrue(rootCause is NativeStackFrame)
        assertEquals("/system/lib/libc.so", rootCause.mapName)
        assertEquals("pthread_mutex_lock", rootCause.functionName)
    }

    @Test
    fun `identify root cause of java crash`() {
        val stackTrace = """
io.reactivex.exceptions.OnErrorNotImplementedException: Error occurred during subscription
        at android.os.Handler.handleCallback(Handler.java:883)
        at android.os.Handler.dispatchMessage(Handler.java:108)
        at android.os.Looper.loop(Looper.java:166)
        at android.app.ActivityThread.main(ActivityThread.java:7529)
        at java.lang.reflect.Method.invoke(Method.java)
        at com.android.internal.os.Zygote${'$'}MethodAndArgsCaller.run(Zygote.java:245)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:921)
Caused by: android.util.AndroidRuntimeException: java.lang.reflect.InvocationTargetException
        at android.webkit.WebViewFactory.getProvider(WebViewFactory.java:271)
        at android.webkit.WebView.getFactory(WebViewFactory.java:2551)
        at io.johnsonlee.example.SimpleWebView.<init>(SimpleWebView.java:80)
        at android.webkit.WebView.ensureProviderCreated(WebView.java:2545)
        at android.webkit.WebView.setOverScrollMode(WebView.java:2634)
        at android.view.View.<init>(View.java:5433)
        at android.view.View.<init>(View.java:5624)
        at android.view.ViewGroup.<init>(ViewGroup.java:687)
        at android.widget.AbsoluteLayout.<init>(AbsoluteLayout.java:58)
        at android.webview.WebView.<init>(WebView.java:410)
        at android.webview.WebView.<init>(WebView.java:353)
        at android.webview.WebView.<init>(WebView.java:336)
Caused by: java.lang.reflect.InvocationTargetException
        at java.lang.reflect.Method.invoke(Native Method)
        at android.webkit.WebViewFactory.getProvider(WebViewFactory.java:271)
        ... 32 more"""
        val rootCause = identifyRootCause(stackTrace)
        assertNotNull(rootCause)
        assertTrue(rootCause is JavaStackFrame)
        assertEquals("SimpleWebView.java", rootCause.sourceFile)
        assertEquals("<init>", rootCause.methodName)
    }

}