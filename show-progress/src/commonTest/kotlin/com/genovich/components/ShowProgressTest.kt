package com.genovich.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ShowProgressTest {

    private val shown = mutableListOf<Progress?>()
    private val showProgress = Action<Progress?, Unit> { shown += it }

    @Test
    fun publishesThenClearsOnSuccess() = runTest {
        val release = CompletableDeferred<Unit>()
        val action = ShowProgress(Action<Int, String> { release.await(); "result $it" }, showProgress)

        val result = async { action(1) }
        runCurrent()
        assertEquals(listOf<Progress?>(Progress), shown)

        release.complete(Unit)
        assertEquals("result 1", result.await())
        assertEquals(listOf(Progress, null), shown)
    }

    @Test
    fun clearsOnFailure() = runTest {
        val action = ShowProgress(Action<Unit, Unit> { error("boom") }, showProgress)

        assertFailsWith<IllegalStateException> { action(Unit) }
        assertEquals(listOf(Progress, null), shown)
    }

    @Test
    fun clearsOnCancellation() = runTest {
        val action = ShowProgress(Action<Unit, Unit> { awaitCancellation() }, showProgress)

        val job = async { action(Unit) }
        runCurrent()
        assertEquals(listOf<Progress?>(Progress), shown)

        job.cancel()
        runCurrent()
        assertEquals(listOf(Progress, null), shown)
    }

    @Test
    fun pairOverloadExposesProgress() = runTest {
        val flow = MutableStateFlow<Progress?>(null)
        val release = CompletableDeferred<Unit>()
        val (action, progress) = ShowProgress(
            Action<Unit, Unit> { release.await() },
            Action<Progress?, Unit> { flow.value = it } to flow,
        )

        val result = async { action(Unit) }
        runCurrent()
        assertEquals(Progress, progress.value)

        release.complete(Unit)
        result.await()
        assertNull(progress.value)
    }
}
