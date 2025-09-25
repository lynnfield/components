package com.genovich.components

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

class Try<TInput, TOutput, T, TError>(
    buildAction: (catch: Action<OneOf<T, TError>, T>) -> Action<TInput, TOutput>,
) : Action<TInput, OneOf<TOutput, TError>> {

    // todo possible race condition???
    private val catchFlow = MutableSharedFlow<TError>()
    private val func = buildAction { result ->
        when (result) {
            is OneOf.First -> result.first
            is OneOf.Second -> {
                catchFlow.emit(result.second)
                awaitCancellation()
            }
        }
    }

    override suspend fun invoke(input: TInput): OneOf<TOutput, TError> {
        return parallel(
            { OneOf.First(func(input)) },
            { OneOf.Second(catchFlow.first()) },
        )
    }
}