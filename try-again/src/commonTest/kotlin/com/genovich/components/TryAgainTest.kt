package com.genovich.components

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TryAgainTest {

    @Test
    fun retriesAfterErrorIsShownAndReturnsSuccess() = runTest {
        val results = ArrayDeque(listOf<OneOf<String, String>>(OneOf.Second("e1"), OneOf.Second("e2"), OneOf.First("ok")))
        val inputs = mutableListOf<Int>()
        val shownErrors = mutableListOf<String>()
        val tryAgain = TryAgain(
            action = Action<Int, OneOf<String, String>> { inputs += it; results.removeFirst() },
            showError = Action<String, Unit> { shownErrors += it },
        )

        assertEquals("ok", tryAgain(7))
        assertEquals(listOf(7, 7, 7), inputs)
        assertEquals(listOf("e1", "e2"), shownErrors)
    }

    @Test
    fun outputMayBeNothingForAnActionThatLoopsForever() = runTest {
        var calls = 0
        val tryAgain: Action<Unit, Nothing> = TryAgain(
            action = Action<Unit, OneOf<Nothing, String>> {
                calls++
                if (calls < 3) OneOf.Second("error $calls") else awaitCancellation()
            },
            showError = Action<String, Unit> { },
        )

        val job = launch { tryAgain(Unit) }
        runCurrent()
        assertEquals(3, calls)
        job.cancel()
    }

    @Test
    fun projectionGoesFromActionToErrorAndBack() = runTest {
        val actionState = MutableStateFlow<UiState<Int, OneOf<String, String>>?>(null)
        val errorState = MutableStateFlow<UiState<String, Unit>?>(null)
        val (tryAgain, projection) = TryAgain(
            action = Show(actionState) to actionState,
            showError = Show(errorState) to errorState,
        )
        val kinds = mutableListOf<String?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            projection.collect { kinds += it?.let { state -> state::class.simpleName } }
        }

        val result = async { tryAgain(1) }
        runCurrent()
        val action = assertIs<TryAgainUiState.Action<UiState<Int, OneOf<String, String>>>>(projection.value)
        assertEquals(1, action.flow.value.input)

        action.flow.value.action(OneOf.Second("failed"))
        runCurrent()
        val error = assertIs<TryAgainUiState.Error<UiState<String, Unit>>>(projection.value)
        assertEquals("failed", error.flow.value.input)

        error.flow.value.action(Unit)
        runCurrent()
        val retried = assertIs<TryAgainUiState.Action<UiState<Int, OneOf<String, String>>>>(projection.value)
        assertNotNull(retried.flow.value).action(OneOf.First("done"))

        assertEquals("done", result.await())
        assertNull(projection.value)
        assertEquals(
            listOf("Action", "Error", "Action"),
            kinds.filterNotNull().fold(listOf<String>()) { acc, kind -> if (acc.lastOrNull() == kind) acc else acc + kind },
        )
    }
}
