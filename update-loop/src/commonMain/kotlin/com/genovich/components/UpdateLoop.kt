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
