package com.genovich.components

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ParallelTest {
    @Test
    fun firstFinishedLaneWins() = runTest {
        val result = parallel({ awaitCancellation() }, { "second" })

        assertEquals("second", result)
    }
}
