package com.genovich.components

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateLoopTest {

    @Test
    fun updateLoopUntilReturnsFirstResult() = runTest {
        val steps = mutableListOf<Int>()

        val result = updateLoopUntil(initial = 0) { state ->
            steps += state
            if (state < 3) OneOf.First(state + 1) else OneOf.Second("done at $state")
        }

        assertEquals("done at 3", result)
        assertEquals(listOf(0, 1, 2, 3), steps)
    }
}
