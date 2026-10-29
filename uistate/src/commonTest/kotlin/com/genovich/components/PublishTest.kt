package com.genovich.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PublishTest {

    @Test
    fun publishSetsAndClears() = runTest {
        val state = MutableStateFlow<String?>(null)
        val publish = Publish(state)

        publish("value")
        assertEquals("value", state.value)

        publish(null)
        assertNull(state.value)
    }

    @Test
    fun pairOverloadExposesPublishedValue() = runTest {
        val (publish, flow) = Publish<Int>()
        assertNull(flow.value)

        publish(1)
        assertEquals(1, flow.value)

        publish(null)
        assertNull(flow.value)
    }
}
