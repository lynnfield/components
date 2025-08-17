@file:OptIn(ExperimentalForInheritanceCoroutinesApi::class)

package com.genovich.components

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine as kotlinCombine
import kotlinx.coroutines.flow.distinctUntilChangedBy as kotlinDistinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull as kotlinFilterNotNull
import kotlinx.coroutines.flow.map as kotlinMap
import kotlinx.coroutines.flow.onEach as kotlinOnEach

fun <T> StateFlow<T>.onEach(action: (T) -> Unit): StateFlow<T> =
    object : StateFlow<T> {
        override val value: T
            get() = this@onEach.value
        override val replayCache: List<T>
            get() = this@onEach.replayCache

        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            this@onEach
                .kotlinOnEach(action)
                .collect(collector)
            error("Should not be reached")
        }
    }

fun <T, R> StateFlow<T>.map(transform: (value: T) -> R): StateFlow<R> =
    object : StateFlow<R> {
        override val value: R
            get() = transform(this@map.value)

        override val replayCache: List<R>
            get() = this@map.replayCache.map(transform)

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            this@map
                .kotlinMap(transform)
                .collect(collector)
            error("Should not be reached")
        }
    }

fun <T, K> StateFlow<T>.distinctUntilChangedBy(keySelector: (T) -> K): StateFlow<T> =
    object : StateFlow<T> {
        override val value: T
            get() = this@distinctUntilChangedBy.value
        override val replayCache: List<T>
            get() = this@distinctUntilChangedBy.replayCache.distinctBy(keySelector)

        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            this@distinctUntilChangedBy
                .kotlinDistinctUntilChangedBy(keySelector)
                .collect(collector)
            error("Should not be reached")
        }
    }

fun <T : Any> StateFlow<T?>.filterNotNull(initial: T): StateFlow<T> =
    object : StateFlow<T> {
        override val value: T
            get() = this@filterNotNull.value ?: initial
        override val replayCache: List<T>
            get() = this@filterNotNull.replayCache.filterNotNull().takeIf { it.isNotEmpty() }
                ?: listOf(initial)

        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            this@filterNotNull
                .kotlinFilterNotNull()
                .collect(collector)
            error("Should not be reached")
        }
    }

fun <T : Any> StateFlow<T?>.emitSelfWhenHaveValue(): StateFlow<StateFlow<T>?> =
    this.distinctUntilChangedBy { value -> value?.let { } }
        .map { it?.let { this.filterNotNull(it) } }

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
    return object : StateFlow<U> {
        override val value: U
            get() = transform(flows.map { it.value }.toTypedArray())

        override val replayCache: List<U>
            get() = List(flows.first().replayCache.size) { index ->
                transform(flows.map { it.replayCache[index] }.toTypedArray())
            }

        override suspend fun collect(collector: FlowCollector<U>): Nothing {
            kotlinCombine(flows.asList(), transform)
                .collect(collector)
            error("Should not be reached")
        }
    }
}