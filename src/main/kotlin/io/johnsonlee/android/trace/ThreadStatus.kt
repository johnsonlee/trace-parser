package io.johnsonlee.android.trace

import kotlin.reflect.KClass

sealed class ThreadStatus(internal val value: String) {

    companion object {

        private val STATES = ThreadStatus::class.sealedSubclasses
                .mapNotNull(KClass<out ThreadStatus>::objectInstance)
                .map { it.value to it }
                .toMap()

        fun valueOf(state: String) = STATES[state] ?: throw IllegalArgumentException("invalid state `${state}`")

    }

    object Terminated : ThreadStatus("Terminated")
    object Runnable : ThreadStatus("Runnable")
    object TimedWaiting : ThreadStatus("TimedWaiting")
    object Sleeping : ThreadStatus("Sleeping")
    object Blocked : ThreadStatus("Blocked")
    object Waiting : ThreadStatus("Waiting")
    object WaitingForLockInflation : ThreadStatus("WaitingForLockInflation")
    object WaitingForTaskProcessor : ThreadStatus("WaitingForTaskProcessor")
    object WaitingForGcToComplete : ThreadStatus("WaitingForGcToComplete")
    object WaitingForCheckPointsToRun : ThreadStatus("WaitingForCheckPointsToRun")
    object WaitingPerformingGc : ThreadStatus("WaitingPerformingGc")
    object WaitingForDebuggerSend : ThreadStatus("WaitingForDebuggerSend")
    object WaitingForDebuggerToAttach : ThreadStatus("WaitingForDebuggerToAttach")
    object WaitingInMainDebuggerLoop : ThreadStatus("WaitingInMainDebuggerLoop")
    object WaitingForDebuggerSuspension : ThreadStatus("WaitingForDebuggerSuspension")
    object WaitingForJniOnLoad : ThreadStatus("WaitingForJniOnLoad")
    object WaitingForSignalCatcherOutput : ThreadStatus("WaitingForSignalCatcherOutput")
    object WaitingInMainSignalCatcherLoop : ThreadStatus("WaitingInMainSignalCatcherLoop")
    object WaitingForDeoptimization : ThreadStatus("WaitingForDeoptimization")
    object WaitingForMethodTracingStart : ThreadStatus("WaitingForMethodTracingStart")
    object WaitingForVisitObjects : ThreadStatus("WaitingForVisitObjects")
    object WaitingForGetObjectsAllocated : ThreadStatus("WaitingForGetObjectsAllocated")
    object WaitingWeakGcRootRead : ThreadStatus("WaitingWeakGcRootRead")
    object WaitingForGcThreadFlip : ThreadStatus("WaitingForGcThreadFlip")
    object NativeForAbort : ThreadStatus("NativeForAbort")
    object Starting : ThreadStatus("Starting")
    object Native : ThreadStatus("Native")
    object Suspended : ThreadStatus("Suspended")

    override fun toString() = value

}