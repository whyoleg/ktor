/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

// ktlint-disable filename
package io.ktor.client.tests.utils

import io.ktor.client.engine.*
import io.ktor.test.dispatcher.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*

private class TestFailure(val name: String, val cause: Throwable) {
    override fun toString(): String = buildString {
        appendLine("Test failed with engine: $name")
        appendLine(cause)
        for (stackline in cause.stackTraceToString().lines()) {
            appendLine("\t$stackline")
        }
    }
}

/**
 * Helper interface to test client.
 */
actual abstract class ClientLoader actual constructor(private val timeoutSeconds: Int) {
    /**
     * Perform test against all clients from dependencies.
     */
    @OptIn(InternalAPI::class)
    actual fun clientTests(
        skipEngines: List<String>,
        onlyWithEngine: String?,
        block: suspend TestClientBuilder<HttpClientEngineConfig>.() -> Unit
    ): TestResult = runTestWithRealTime {
        val skipEnginesLowerCase = skipEngines.map { it.lowercase() }.toSet()
        if (skipEnginesLowerCase.any { it.startsWith("js") }) return@runTestWithRealTime

        val filteredEngines: List<HttpClientEngineFactory<HttpClientEngineConfig>> = engines.filter {
            val name = it.toString().lowercase()
            !skipEnginesLowerCase.contains(name) && !skipEnginesLowerCase.contains("js:$name")
        }

        val failures = mutableListOf<TestFailure>()
        for (engine in filteredEngines) {
            if (onlyWithEngine != null && onlyWithEngine != engine.toString()) continue

            val result = runCatching {
                doTestWithEngine(engine) {
                    withTimeout(timeoutSeconds.toLong() * 1000L) {
                        block()
                    }
                }
            }

            if (result.isFailure) {
                failures += TestFailure(engine.toString(), result.exceptionOrNull()!!)
            }
        }

        if (failures.isEmpty()) return@runTestWithRealTime

        error(failures.joinToString("\n"))
    }

    actual fun dumpCoroutines() {
        error("Debug probes unsupported[js]")
    }
}
