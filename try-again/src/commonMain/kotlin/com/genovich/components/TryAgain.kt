package com.genovich.components

import kotlinx.coroutines.flow.StateFlow

/**
 * Runs [action] until it succeeds.
 * When [action] returns [OneOf.Second], the error goes to [showError],
 * and [action] runs again once [showError] returns (the user asked to retry).
 * [Output] may be [Nothing] for an [action] that loops forever after its success.
 * */
class TryAgain<Input, Output, Error>(
    val action: Action<Input, OneOf<Output, Error>>,
    val showError: Action<Error, Unit>,
) : Action<Input, Output>() {

    override suspend fun invoke(input: Input): Output {
        return updateLoopUntil<Unit, Output>(Unit) {
            when (val result = action(input)) {
                is OneOf.First -> OneOf.Second(result.first)
                is OneOf.Second -> {
                    showError(result.second)
                    OneOf.First(Unit)
                }
            }
        }
    }
}

sealed class TryAgainUiState<out ActionUiState : Any, out ErrorUiState : Any> {
    class Action<ActionUiState : Any>(val flow: StateFlow<ActionUiState>) : TryAgainUiState<ActionUiState, Nothing>()
    class Error<ErrorUiState : Any>(val flow: StateFlow<ErrorUiState>) : TryAgainUiState<Nothing, ErrorUiState>()
}

/**
 * [TryAgain] wired with a projection of its UI state:
 * [TryAgainUiState.Error] while the error is shown, otherwise [TryAgainUiState.Action] while [action] shows something.
 * */
fun <Input, Output, Error, ActionUiState : Any> TryAgain(
    action: Pair<Action<Input, OneOf<Output, Error>>, StateFlow<ActionUiState?>>,
    showError: Pair<Action<Error, Unit>, StateFlow<UiState<Error, Unit>?>>,
): Pair<Action<Input, Output>, StateFlow<TryAgainUiState<ActionUiState, UiState<Error, Unit>>?>> {
    return TryAgain(action.first, showError.first) to combine(
        flow = action.second.emitSelfWhenHaveValue(),
        flow2 = showError.second.emitSelfWhenHaveValue(),
        transform = { actionFlow, errorFlow ->
            if (errorFlow != null) {
                TryAgainUiState.Error(errorFlow)
            } else if (actionFlow != null) {
                TryAgainUiState.Action(actionFlow)
            } else {
                null
            }
        }
    )
}
