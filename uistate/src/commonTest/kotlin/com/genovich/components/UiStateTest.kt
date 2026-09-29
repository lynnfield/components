package com.genovich.components

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UiStateTest {

    @Test
    fun answeringResumesAndClearsState() = runTest {
        val state = MutableStateFlow<UiState<String, Int>?>(null)
        val show = Show(state)

        val result = async { show("question") }
        runCurrent()
        val request = assertNotNull(state.value)
        assertEquals("question", request.input)

        request.action(42)

        assertEquals(42, result.await())
        assertNull(state.value)
    }

    @Test
    fun staleCallbackCalledTwiceIsIgnored() = runTest {
        val state = MutableStateFlow<UiState<String, Int>?>(null)
        val show = Show(state)

        val first = async { show("first") }
        runCurrent()
        val staleAnswer = assertNotNull(state.value).action

        staleAnswer(1)
        assertEquals(1, first.await())

        // A newer request is on screen when the stale callback fires again (double tap).
        val second = async { show("second") }
        runCurrent()
        staleAnswer(2)

        val current = assertNotNull(state.value)
        assertEquals("second", current.input)
        current.action(3)
        assertEquals(3, second.await())
    }
}
