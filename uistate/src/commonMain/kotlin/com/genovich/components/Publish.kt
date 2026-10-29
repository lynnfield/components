package com.genovich.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * A one-way channel from a logic "layer" to another asynchronous system.
 * Like [Show], but there is no callback: it publishes the value (or clears it with `null`)
 * and returns immediately instead of suspending for an answer.
 * */
data class Publish<T : Any>(
    val update: (T?) -> Unit
) : Action<T?, Unit>() {

    constructor(
        flow: MutableStateFlow<T?>
    ) : this({ value -> flow.update { value } })

    override suspend fun invoke(input: T?) {
        update(input)
    }
}

/**
 * Creates a [Publish] together with the [StateFlow] it publishes to.
 * */
fun <T : Any> Publish(): Pair<Action<T?, Unit>, StateFlow<T?>> {
    val flow = MutableStateFlow<T?>(null)
    return Publish(flow) to flow
}
