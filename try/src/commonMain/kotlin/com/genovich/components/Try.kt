package com.genovich.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation

class Try<TInput, TOutput, T, TError>(
    val buildAction: (catch: Action<OneOf<T, TError>, T>) -> Action<TInput, TOutput>,
) : Action<TInput, OneOf<TOutput, TError>>() {

    override suspend fun invoke(input: TInput): OneOf<TOutput, TError> {
        // A deferred keeps the error even if the body reaches `catch` before the error lane
        // below starts awaiting it (a SharedFlow without subscribers would drop it and hang).
        val caught = CompletableDeferred<TError>()
        val func = buildAction(object : Action<OneOf<T, TError>, T>() {
            override suspend fun invoke(input: OneOf<T, TError>): T {
                return when (input) {
                    is OneOf.First -> input.first
                    is OneOf.Second -> {
                        caught.complete(input.second)
                        awaitCancellation()
                    }
                }
            }
        })
        return parallel(
            { OneOf.First(func(input)) },
            { OneOf.Second(caught.await()) },
        )
    }
}
