package com.genovich.components

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext

/**
 * A badly-named request from a logic "layer" to another asynchronous system.
 * It is expected that requesting side is forming [UiState] of a context (state)
 * represented as [Input] type and a callback that the responding side should call
 * providing [Output] in order to respond for the request.
 * */
 // should be a class for better Obj-C compatibility
class UiState<Input, Output>(val value: Pair<Input, (Output) -> Unit>) {

    val input: Input get() = value.first

    val action: (Output) -> Unit get() = value.second

    constructor(input: Input, action: (Output) -> Unit) : this(input to action)

    operator fun component1() = input
    operator fun component2() = action
}

data class Show<Input, Output>(
    val mutableStateFlow: MutableStateFlow<UiState<Input, Output>?>
) : Action<Input, Output>() {
    override suspend fun invoke(input: Input): Output {
        return mutableStateFlow.showAndGetResult(input)
    }
}


typealias UiStateFlow<Input, Output> = StateFlow<UiState<Input, Output>?>

typealias UiStateBinding<Input, Output> = Pair<Action<Input, Output>, UiStateFlow<Input, Output>>

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
fun <Input, Output> Show(): UiStateBinding<Input, Output> {
    val mutableStateFlow = object : MutableStateFlow<UiState<Input, Output>?>, MutableSharedFlow<UiState<Input, Output>?> by MutableSharedFlow(replay = 1, extraBufferCapacity = 4, onBufferOverflow = BufferOverflow.DROP_OLDEST) {

        override var value: UiState<Input, Output>? = null
            set(value) { tryEmit(value) }

        override fun compareAndSet(
            expect: UiState<Input, Output>?,
            update: UiState<Input, Output>?
        ): Boolean {
            if (expect != value) return false
            value = update
            return true
        }
    }
    return Show(mutableStateFlow) to mutableStateFlow
}

suspend fun <T, U> MutableStateFlow<UiState<T, U>?>.showAndGetResult(input: T): U {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { value = null }
        value = UiState(input, continuation.asCallback(this))
    }
}

fun <T, U : Any> CancellableContinuation<T>.asCallback(flow: MutableStateFlow<U?>): (T) -> Unit =
    { value: T ->
        flow.value = null
        resume(value) { cause, resumedValue, coroutineContext ->
            coroutineContext[Logger]?.log(
                "cannot resume with $resumedValue, because continuation is cancelled",
                cause
            )
        }
    }

// todo move it to it's own module
interface Logger : CoroutineContext.Element {

    companion object Key : CoroutineContext.Key<Logger>

    fun log(message: String, cause: Throwable? = null)

    override val key: CoroutineContext.Key<*> get() = Logger
}
