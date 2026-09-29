@file:OptIn(ExperimentalForInheritanceCoroutinesApi::class)

package com.genovich.components

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class Tests {
    @Test
    fun emitSelfWhenHaveValue() = runTest {
        val source = object : StateFlow<String?> {
            override val value: String? get() = null
            override val replayCache: List<String?> get() = emptyList()
            override suspend fun collect(collector: FlowCollector<String?>): Nothing {
                collector.emit(null)
                collector.emit("1")
                collector.emit(null)
                awaitCancellation()
            }
        }
        val destination = source.emitSelfWhenHaveValue()

        backgroundScope.async { destination.collect { println("value: $it") } }

        val collection = backgroundScope.async { destination.take(3).toList() }

        val actual = collection.await()

        assertNull(actual[0])
        assertNotNull(actual[1])
        assertNull(actual[2])
    }

    @Test
    fun distinctUntilChanged() = runTest {
        val source = object : StateFlow<String?> {
            override val value: String? get() = null
            override val replayCache: List<String?> get() = emptyList()
            override suspend fun collect(collector: FlowCollector<String?>): Nothing {
                collector.emit(null)
                collector.emit("1")
                collector.emit("1")
                collector.emit(null)
                collector.emit(null)
                awaitCancellation()
            }
        }
        val destination = source.distinctUntilChangedBy { it }

        backgroundScope.async { destination.collect { println("value: $it") } }
        val collection = backgroundScope.async { destination.take(3).toList() }


        val actual = collection.await()

        assertEquals(listOf(null, "1", null), actual)
    }
}
class GeneratedShapeTests {
    @Test
    fun varargCombinePassesValuesInOrder() {
        val a = kotlinx.coroutines.flow.MutableStateFlow(1)
        val b = kotlinx.coroutines.flow.MutableStateFlow(2)
        val c = kotlinx.coroutines.flow.MutableStateFlow(3)

        val combined = combine(a, b, c) { values -> values.joinToString() }
        assertEquals("1, 2, 3", combined.value)

        b.value = 5
        assertEquals("1, 5, 3", combined.value)
    }

    @Test
    fun emitSelfWhenHaveValueWraps() {
        val source = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
        val wrapped = emitSelfWhenHaveValue(source) { flow -> "wrapped:" + flow.value }

        assertNull(wrapped.value)
        source.value = "x"
        assertEquals("wrapped:x", wrapped.value)
    }
}
