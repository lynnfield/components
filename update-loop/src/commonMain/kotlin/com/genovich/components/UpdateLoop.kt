package com.genovich.components


/**
 * Executes [step] to update [initial] in a loop.
 * Each next invocation of [step] takes the result of the previous one.
 * Never returns normally.
 * */
suspend inline fun <T> updateLoop(initial: T, step: (T) -> T): Nothing {
    var current = initial
    whileActive {
        current = step(current)
    }
}

/**
 * Executes [step] to update [initial] in a loop until it produces a result.
 * [step] returns [OneOf.First] with the next state to keep looping,
 * or [OneOf.Second] with the result to return.
 * */
suspend inline fun <S, R> updateLoopUntil(initial: S, step: (S) -> OneOf<S, R>): R {
    var current = initial
    whileActive {
        when (val next = step(current)) {
            is OneOf.First -> current = next.first
            is OneOf.Second -> return next.second
        }
    }
}
