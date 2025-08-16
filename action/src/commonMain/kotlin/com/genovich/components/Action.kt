package com.genovich.components

interface Action<in Input, out Output> {
    suspend operator fun invoke(input: Input): Output
}
