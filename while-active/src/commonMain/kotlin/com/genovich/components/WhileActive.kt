package com.genovich.components

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext


/**
 * Executes [block] while the coroutine is active.
 * Never returns normally.
 * */
suspend inline fun whileActive(block: () -> Unit): Nothing {
    while (coroutineContext.isActive) {
        block()
    }
    awaitCancellation()
}
