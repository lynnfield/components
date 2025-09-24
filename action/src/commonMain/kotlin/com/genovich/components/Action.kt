package com.genovich.components

fun interface Action<in Input, out Output> {
    suspend operator fun invoke(input: Input): Output
}
