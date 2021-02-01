package io.johnsonlee.android.trace

import kotlin.reflect.KClass

sealed class ThreadState(internal val value: String) {

    companion object {

        private val STATES = ThreadState::class.sealedSubclasses
                .mapNotNull(KClass<out ThreadState>::objectInstance)
                .map { it.value to it }
                .toMap()

        fun valueOf(state: String) = STATES[state] ?: throw IllegalArgumentException("invalid state `${state}`")

    }

    object Terminated : ThreadState("Terminated")
    object Runnable : ThreadState("Runnable")
    object TimedWaiting : ThreadState("TimedWaiting")
    object Sleeping : ThreadState("Sleeping")
    object Blocked : ThreadState("Blocked")
    object Waiting : ThreadState("Waiting")
    object WaitingForLockInflation : ThreadState("WaitingForLockInflation")
    object WaitingForTaskProcessor : ThreadState("WaitingForTaskProcessor")
    object WaitingForGcToComplete : ThreadState("WaitingForGcToComplete")
    object WaitingForCheckPointsToRun : ThreadState("WaitingForCheckPointsToRun")
    object WaitingPerformingGc : ThreadState("WaitingPerformingGc")
    object WaitingForDebuggerSend : ThreadState("WaitingForDebuggerSend")
    object WaitingForDebuggerToAttach : ThreadState("WaitingForDebuggerToAttach")
    object WaitingInMainDebuggerLoop : ThreadState("WaitingInMainDebuggerLoop")
    object WaitingForDebuggerSuspension : ThreadState("WaitingForDebuggerSuspension")
    object WaitingForJniOnLoad : ThreadState("WaitingForJniOnLoad")
    object WaitingForSignalCatcherOutput : ThreadState("WaitingForSignalCatcherOutput")
    object WaitingInMainSignalCatcherLoop : ThreadState("WaitingInMainSignalCatcherLoop")
    object WaitingForDeoptimization : ThreadState("WaitingForDeoptimization")
    object WaitingForMethodTracingStart : ThreadState("WaitingForMethodTracingStart")
    object WaitingForVisitObjects : ThreadState("WaitingForVisitObjects")
    object WaitingForGetObjectsAllocated : ThreadState("WaitingForGetObjectsAllocated")
    object WaitingWeakGcRootRead : ThreadState("WaitingWeakGcRootRead")
    object WaitingForGcThreadFlip : ThreadState("WaitingForGcThreadFlip")
    object NativeForAbort : ThreadState("NativeForAbort")
    object Starting : ThreadState("Starting")
    object Native : ThreadState("Native")
    object Suspended : ThreadState("Suspended")

    override fun toString() = value

}