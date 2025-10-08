package com.genovich.components

// should be a class for better Obj-C compatibility
abstract class Action<in Input, out Output> : ActionBase<Input, Output>

fun interface ActionBase<in Input, out Output> {
    suspend operator fun invoke(input: Input): Output
}