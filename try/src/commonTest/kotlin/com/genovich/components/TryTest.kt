package com.genovich.components

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals

class TryTest {

    @Test
    fun returnsBodyResult() = runTest {
        val action = Try<Int, String, Int, String> { catch ->
            Action { input -> catch(OneOf.First(input)).toString() }
        }

        assertEquals(OneOf.First("1"), action(1))
    }

    @Test
    fun errorRaisedBeforeErrorLaneSubscribesIsNotLost() = runTest {
        // The body reaches `catch` synchronously, before the error lane has started.
        val action = Try<Unit, Unit, Unit, String> { catch ->
            Action { catch(OneOf.Second("boom")) }
        }

        val result = withTimeout(1_000) { action(Unit) }

        assertEquals(OneOf.Second("boom"), result)
    }
}
