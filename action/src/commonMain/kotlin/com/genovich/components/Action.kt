package com.genovich.components

// should be a class for better Obj-C compatibility
abstract class Action<in Input, out Output> : ActionBase<Input, Output>

fun interface ActionBase<in Input, out Output> {
    suspend operator fun invoke(input: Input): Output
}

/**
 * Creates an [Action] from a suspend lambda, e.g. `Action<Int, String> { it.toString() }`.
 * */
fun <Input, Output> Action(block: suspend (input: Input) -> Output): Action<Input, Output> =
    object : Action<Input, Output>() {
        override suspend fun invoke(input: Input): Output = block(input)
    }
