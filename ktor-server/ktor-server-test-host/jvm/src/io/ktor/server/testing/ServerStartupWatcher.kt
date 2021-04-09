/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.testing

import io.ktor.server.engine.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import java.io.*
import java.net.*
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.coroutines.*

internal suspend fun waitForStartup(server: ApplicationEngine, timeout: Long) {
    withTimeout(TimeUnit.SECONDS.toMillis(minOf(10, timeout))) {
        val ports = suspendCancellableCoroutine<List<Int>> { continuation ->
            val handle = server.environment.monitor.subscribe(EngineConnectorStarted) { event ->
                if (server.environment.connectorsConfig.size == server.environment.startedConnectors.size) {
                    continuation.resume(server.environment.startedConnectors.map { it.port })
                }
            }

            continuation.invokeOnCancellation {
                handle.dispose()
            }

            if (server.environment.connectorsConfig.size == server.environment.startedConnectors.size) {
                continuation.resume(server.environment.startedConnectors.map { it.port })
                return@suspendCancellableCoroutine
            }
        }

        ports.forEach { port ->
            waitForPort(port)
        }
    }
}

private suspend fun waitForPort(port: Int) {
    do {
        delay(50)
        try {
            @Suppress("BlockingMethodInNonBlockingContext")
            Socket("localhost", port).close()
            break
        } catch (expected: IOException) {
        }
    } while (true)
}
