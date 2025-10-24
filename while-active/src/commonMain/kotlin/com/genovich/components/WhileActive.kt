package com.genovich.components

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive


/**
 * Executes [block] while the coroutine is active.
 * Never returns normally.
 * */
suspend inline fun whileActive(block: () -> Unit): Nothing {
    while (currentCoroutineContext().isActive) {
        block()
    }
    awaitCancellation()
}
