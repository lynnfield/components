package com.genovich.components

/**
 * An implementation of Either
 * */
// should be a class for better Obj-C compatibility
sealed class OneOf<out First, out Second> {
    data class First<T>(val first: T) : OneOf<T, Nothing>()
    data class Second<T>(val second: T) : OneOf<Nothing, T>()
}