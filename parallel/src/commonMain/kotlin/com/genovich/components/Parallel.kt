package com.genovich.components

import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select

/**
 * Run [fun1] and [fun2] in parallel.
 * */
suspend fun <Output> parallel(
    fun1: suspend () -> Output,
    fun2: suspend () -> Output,
): Output = coroutineScope {
    select {
        async { fun1() }.onAwait { it }
        async { fun2() }.onAwait { it }
    }.also {
        coroutineContext.cancelChildren()
    }
}

/**
 * Build a function that runs [fun1] and [fun2] in parallel.
 * */
fun <Output> buildParallel(
    fun1: suspend () -> Output,
    fun2: suspend () -> Output,
): suspend () -> Output {
    return { parallel(fun1, fun2) }
}

/**
 * Run [fun1] and [fun2] in parallel.
 * Feeds [input] to [fun1] and [fun2].
 * */
suspend fun <Input, Output> parallel(
    input: Input,
    fun1: suspend (Input) -> Output,
    fun2: suspend (Input) -> Output
): Output = coroutineScope {
    select {
        async { fun1(input) }.onAwait { it }
        async { fun2(input) }.onAwait { it }
    }.also {
        coroutineContext.cancelChildren()
    }
}

/**
 * Build a function that runs [fun1] and [fun2] in parallel.
 * Feeds [Input] to [fun1] and [fun2].
 * */
fun <Input, Output> buildParallel(
    fun1: suspend (Input) -> Output,
    fun2: suspend (Input) -> Output
): suspend (Input) -> Output {
    return { input -> parallel(input, fun1, fun2) }
}
