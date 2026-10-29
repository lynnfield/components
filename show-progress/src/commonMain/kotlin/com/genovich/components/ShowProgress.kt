package com.genovich.components

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * A marker telling the UI that something is in progress.
 * */
data object Progress

/**
 * Runs [original], showing [Progress] while it runs.
 * The progress is cleared when [original] returns, fails or is cancelled.
 * */
class ShowProgress<Input, Output>(
    val original: Action<Input, Output>,
    val showProgress: Action<Progress?, Unit>,
) : Action<Input, Output>() {

    override suspend fun invoke(input: Input): Output {
        return try {
            showProgress(Progress)
            original(input)
        } finally {
            withContext(NonCancellable) { showProgress(null) }
        }
    }
}

/**
 * [ShowProgress] wired with the [StateFlow] that [showProgress] publishes to.
 * */
fun <Input, Output> ShowProgress(
    original: Action<Input, Output>,
    showProgress: Pair<Action<Progress?, Unit>, StateFlow<Progress?>>,
): Pair<Action<Input, Output>, StateFlow<Progress?>> {
    return ShowProgress(original, showProgress.first) to showProgress.second
}
