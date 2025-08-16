@file:OptIn(ExperimentalForInheritanceCoroutinesApi::class)

package com.genovich.components

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.map

fun <T, R> StateFlow<T>.map(transform: (value: T) -> R): StateFlow<R> =
    object : StateFlow<R> by MutableStateFlow(transform(value)) {
        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            this@map.collect { collector.emit(transform(it)) }
        }
    }

fun <T, K> StateFlow<T>.distinctUntilChangedBy(keySelector: (T) -> K): StateFlow<T> =
    object : StateFlow<T> by MutableStateFlow(value) {

        private val nullValue = Any()

        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            var previousKey: Any? = nullValue
            this@distinctUntilChangedBy.collect {
                val newKey = keySelector(it)
                @Suppress("SuspiciousEqualsCombination") if (previousKey === nullValue || previousKey != newKey) {
                    previousKey = newKey
                    collector.emit(it)
                }
            }
        }
    }

fun <T : Any> StateFlow<T?>.filterNotNull(initial: T): StateFlow<T> =
    object : StateFlow<T> by MutableStateFlow(initial) {
        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            this@filterNotNull.collect { if (it != null) collector.emit(it) }
        }
    }

fun <T : Any> StateFlow<T?>.emitSelfWhenHaveValue(): StateFlow<StateFlow<T>?> =
    this.distinctUntilChangedBy { it?.let { } }.map { it?.let { this.filterNotNull(it) } }

@Suppress("UNCHECKED_CAST")
fun <T1, T2, R> combine(
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    transform: (T1, T2) -> R,
): StateFlow<R> {
    return combineInternal(arrayOf(flow, flow2)) { results ->
        transform(
            results[0] as T1,
            results[1] as T2,
        )
    }
}

@Suppress("UNCHECKED_CAST")
fun <T1, T2, T3, R> combine(
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    transform: (T1, T2, T3) -> R,
): StateFlow<R> {
    return combineInternal(arrayOf(flow, flow2, flow3)) { results ->
        transform(
            results[0] as T1,
            results[1] as T2,
            results[2] as T3,
        )
    }
}

@Suppress("UNCHECKED_CAST")
fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    flow4: StateFlow<T4>,
    flow5: StateFlow<T5>,
    flow6: StateFlow<T6>,
    transform: (T1, T2, T3, T4, T5, T6) -> R,
): StateFlow<R> {
    return combineInternal(arrayOf(flow, flow2, flow3, flow4, flow5, flow6)) { results ->
        transform(
            results[0] as T1,
            results[1] as T2,
            results[2] as T3,
            results[3] as T4,
            results[4] as T5,
            results[5] as T6,
        )
    }
}

private inline fun <reified T, U> combineInternal(
    flows: Array<StateFlow<T>>,
    crossinline transform: (Array<T>) -> U,
): StateFlow<U> {
    val nullValue = Any()
    return object : StateFlow<U> by MutableStateFlow(
        transform(flows.map { it.value }.toTypedArray())
    ) {
        override suspend fun collect(collector: FlowCollector<U>): Nothing {
            coroutineScope {
                val flowValues = Array<Any?>(flows.size) { nullValue }

                flows.forEachIndexed { index, flow ->
                    launch {
                        flow.collect {
                            flowValues[index] = it
                            if (flowValues.all { it !== nullValue }) {
                                collector.emit(transform(flowValues.map { it as T }
                                    .toTypedArray<T>()))
                            }
                        }
                    }
                }
            }
            error("Should not be reached")
        }
    }
}