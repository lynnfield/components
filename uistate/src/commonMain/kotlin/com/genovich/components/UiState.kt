package com.genovich.components

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmInline


/**
 * A badly-named request from a logic "layer" to another asynchronous system.
 * It is expected that requesting side is forming [UiState] of a context (state)
 * represented as [Input] type and a callback that the responding side should call
 * providing [Output] in order to respond for the request.
 * */
@JvmInline
value class UiState<Input, Output>(val value: Pair<Input, (Output) -> Unit>) {

    val input: Input get() = value.first

    val action: (Output) -> Unit get() = value.second

    constructor(input: Input, action: (Output) -> Unit) : this(input to action)

    operator fun component1() = input
    operator fun component2() = action
}

data class Show<Input, Output>(
    val mutableStateFlow: MutableStateFlow<UiState<Input, Output>?>
) : suspend (Input) -> Output {
    override suspend fun invoke(input: Input): Output {
        return mutableStateFlow.showAndGetResult(input)
    }
}

suspend fun <T, U> MutableStateFlow<UiState<T, U>?>.showAndGetResult(input: T): U {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { value = null }
        value = UiState(input, continuation.asCallback())
    }
}

fun <T> CancellableContinuation<T>.asCallback(): (T) -> Unit = { value: T ->
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
