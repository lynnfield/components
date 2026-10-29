package com.genovich.components

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Waits for [open] to answer, then builds the feature with [featureBuilder] and runs it
 * in parallel with [close]: whichever returns first gives the result and cancels the other.
 * The feature's UI state is published to [outputFlow] while the feature runs.
 * */
class OnDemandFeature<Input, Output, FeatureInput, FeatureUiState : Any>(
    val open: Action<Input, FeatureInput>,
    val close: Action<Input, Output>,
    val featureBuilder: (Input) -> Pair<Action<FeatureInput, Output>, StateFlow<FeatureUiState?>>,
    val outputFlow: MutableStateFlow<StateFlow<FeatureUiState>?>,
) : Action<Input, Output>() {

    override suspend fun invoke(input: Input): Output {
        val featureInput = open(input) // wait for an external action
        val (feature, flow) = featureBuilder(input)
        return coroutineScope {
            val job = launch {
                flow
                    .emitSelfWhenHaveValue()
                    .collect { outputFlow.value = it }
            }
            try {
                parallel(
                    { feature(featureInput) },
                    { close(input) },
                )
            } finally {
                outputFlow.value = null
                job.cancelAndJoin()
            }
        }
    }
}

/**
 * [OnDemandFeature] wired with the projection of its UI state, see [OnDemandFeatureUiStateFlow].
 * */
fun <Input, Output, FeatureInput, OpenUiState : Any, CloseUiState : Any, FeatureUiState : Any> OnDemandFeature(
    open: Pair<Action<Input, FeatureInput>, StateFlow<OpenUiState?>>,
    close: Pair<Action<Input, Output>, StateFlow<CloseUiState?>>,
    featureBuilder: (Input) -> Pair<Action<FeatureInput, Output>, StateFlow<FeatureUiState?>>,
): Pair<Action<Input, Output>, OnDemandFeatureUiStateFlow<OpenUiState, CloseUiState, FeatureUiState>> {
    val outputFlow: MutableStateFlow<StateFlow<FeatureUiState>?> = MutableStateFlow(null)
    return OnDemandFeature(
        open.first,
        close.first,
        featureBuilder,
        outputFlow,
    ) to OnDemandFeatureUiStateFlow(
        open.second, close.second, outputFlow
    )
}

sealed class OnDemandFeatureUiState<out OpenUiState : Any, out CloseUiState : Any, out FeatureUiState : Any> {
    class Open<OpenUiState : Any>(val flow: StateFlow<OpenUiState>) : OnDemandFeatureUiState<OpenUiState, Nothing, Nothing>()
    class FeatureAndClose<CloseUiState : Any, FeatureUiState : Any>(
        val featureFlow: StateFlow<FeatureUiState>,
        val closeFlow: StateFlow<CloseUiState>,
    ) : OnDemandFeatureUiState<Nothing, CloseUiState, FeatureUiState>()
}

/**
 * [OnDemandFeatureUiState.Open] while only [openUiStateFlow] has a value,
 * [OnDemandFeatureUiState.FeatureAndClose] while the feature runs next to [closeUiStateFlow],
 * otherwise `null`.
 * */
class OnDemandFeatureUiStateFlow<OpenUiState : Any, CloseUiState : Any, FeatureUiState : Any>(
    val openUiStateFlow: StateFlow<OpenUiState?>,
    val closeUiStateFlow: StateFlow<CloseUiState?>,
    val featureUiStateFlow: StateFlow<StateFlow<FeatureUiState>?>,
) : StateFlow<OnDemandFeatureUiState<OpenUiState, CloseUiState, FeatureUiState>?> by combine(
    flow = openUiStateFlow.emitSelfWhenHaveValue(),
    flow2 = closeUiStateFlow.emitSelfWhenHaveValue(),
    flow3 = featureUiStateFlow,
    transform = { openUiStateFlow, closeUiStateFlow, featureUiStateFlow ->
        if (openUiStateFlow != null && closeUiStateFlow == null && featureUiStateFlow == null) {
            OnDemandFeatureUiState.Open(openUiStateFlow)
        } else if (openUiStateFlow == null && closeUiStateFlow != null && featureUiStateFlow != null) {
            OnDemandFeatureUiState.FeatureAndClose(featureUiStateFlow, closeUiStateFlow)
        } else {
            null
        }
    }
)
