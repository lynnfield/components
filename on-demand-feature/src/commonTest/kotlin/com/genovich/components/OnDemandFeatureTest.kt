package com.genovich.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnDemandFeatureTest {

    private val openState = MutableStateFlow<UiState<String, Int>?>(null)
    private val closeState = MutableStateFlow<UiState<String, String>?>(null)
    private val featureState = MutableStateFlow<String?>(null)

    @Test
    fun featureIsBuiltOnlyAfterOpenAnswers() = runTest {
        val builtWith = mutableListOf<String>()
        val (onDemand, _) = OnDemandFeature(
            open = Show(openState) to openState,
            close = Show(closeState) to closeState,
            featureBuilder = { input: String ->
                builtWith += input
                Action<Int, String> { awaitCancellation() } to featureState
            },
        )

        val result = async { onDemand("input") }
        runCurrent()
        assertEquals(emptyList(), builtWith)

        assertNotNull(openState.value).action(1)
        runCurrent()
        assertEquals(listOf("input"), builtWith)

        result.cancel()
    }

    @Test
    fun featureResultReturns() = runTest {
        val finish = CompletableDeferred<Unit>()
        val (onDemand, _) = OnDemandFeature(
            open = Show(openState) to openState,
            close = Show(closeState) to closeState,
            featureBuilder = { _: String ->
                Action<Int, String> { finish.await(); "feature $it" } to featureState
            },
        )

        val result = async { onDemand("input") }
        runCurrent()
        assertNotNull(openState.value).action(5)
        runCurrent()

        finish.complete(Unit)
        assertEquals("feature 5", result.await())
        assertNull(closeState.value)
    }

    @Test
    fun closeCancelsFeatureAndClearsItsFlow() = runTest {
        val outputFlow = MutableStateFlow<StateFlow<String>?>(null)
        var featureCancelled = false
        val onDemand = OnDemandFeature(
            open = Show(openState),
            close = Show(closeState),
            featureBuilder = { _: String ->
                Action<Int, String> {
                    try {
                        featureState.value = "feature"
                        awaitCancellation()
                    } finally {
                        featureCancelled = true
                    }
                } to featureState
            },
            outputFlow = outputFlow,
        )

        val result = async { onDemand("input") }
        runCurrent()
        assertNotNull(openState.value).action(1)
        runCurrent()
        assertEquals("feature", assertNotNull(outputFlow.value).value)

        assertNotNull(closeState.value).action("closed")
        assertEquals("closed", result.await())
        assertTrue(featureCancelled)
        assertNull(outputFlow.value)
    }

    @Test
    fun projectionGoesFromOpenToFeatureAndCloseAndBack() = runTest {
        val (onDemand, projection) = OnDemandFeature(
            open = Show(openState) to openState,
            close = Show(closeState) to closeState,
            featureBuilder = { _: String ->
                val state = MutableStateFlow<UiState<Int, String>?>(null)
                Show(state) to state
            },
        )
        val kinds = mutableListOf<String?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            projection.collect { kinds += it?.let { state -> state::class.simpleName } }
        }

        val first = async { onDemand("input") }
        runCurrent()
        val open = assertIs<OnDemandFeatureUiState.Open<UiState<String, Int>>>(projection.value)
        assertEquals("input", open.flow.value.input)

        open.flow.value.action(3)
        runCurrent()
        val featureAndClose =
            assertIs<OnDemandFeatureUiState.FeatureAndClose<UiState<String, String>, UiState<Int, String>>>(projection.value)
        assertEquals(3, featureAndClose.featureFlow.value.input)
        assertEquals("input", featureAndClose.closeFlow.value.input)

        featureAndClose.featureFlow.value.action("done")
        assertEquals("done", first.await())
        runCurrent()
        assertNull(projection.value)

        val second = async { onDemand("again") }
        runCurrent()
        assertIs<OnDemandFeatureUiState.Open<UiState<String, Int>>>(projection.value)
        second.cancel()

        assertEquals(
            listOf("Open", "FeatureAndClose", "Open"),
            kinds.filterNotNull().fold(listOf<String>()) { acc, kind -> if (acc.lastOrNull() == kind) acc else acc + kind },
        )
    }
}
