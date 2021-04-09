/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.testing.suites

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import kotlin.test.*

abstract class RandomPortTestSuite<TEngine : ApplicationEngine,
    TConfiguration : ApplicationEngine.Configuration>(hostFactory: ApplicationEngineFactory<TEngine, TConfiguration>) :
    EngineTestBase<TEngine, TConfiguration>(hostFactory) {

    init {
        enableHttp2 = false
        enableSsl = false
    }

    @Test
    fun testStart() {
        var actualPort = -1

        createAndStartServer {
            application.environment.monitor.subscribe(EngineConnectorStarted) {
                actualPort = application.environment.startedConnectors.single().port
            }

            get("/q") {
                call.respondText("Actual port is $actualPort, ${call.request.origin.port}")
            }
        }

        assertEquals(
            0,
            (server!!.application.environment as ApplicationEngineEnvironment)
                .connectorsConfig.single().port
        )
        assertNotEquals(-1, actualPort)

        runBlocking {
            HttpClient().use { client ->
                assertEquals(
                    "Actual port is $actualPort, $actualPort",
                    client.get<String>("http://localhost:$actualPort/q")
                )
            }
        }
    }

    override fun findFreePort(): Int {
        return 0
    }
}
