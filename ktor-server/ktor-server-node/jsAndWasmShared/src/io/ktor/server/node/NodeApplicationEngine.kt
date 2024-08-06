/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.node

import io.ktor.events.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.node.external.node.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.io.*

public class NodeApplicationEngine(
    environment: ApplicationEnvironment,
    monitor: Events,
    developmentMode: Boolean,
    public val configuration: Configuration,
    private val applicationProvider: () -> Application
) : BaseApplicationEngine(environment, monitor, developmentMode) {
    public class Configuration : BaseApplicationEngine.Configuration()

    private val servers by lazy {
        configuration.connectors.map { connector ->
            createServer { req, res ->
                val application = applicationProvider()
                application.launch {
                    val output = ByteChannel()
                    launch {
                        while (output.awaitContent()) {
                            output.readAvailable(1) {
                                res.write(it.readByteArray().toUint8Array()) {
                                    // coroutine support
                                }
                                0
                            }
                        }
                        res.end {
                            // coroutine support
                        }
                    }

                    pipeline.execute(NodeApplicationCall(application, req, res, output), Unit)
                }
            }.listen(connector.port, connector.host) {
                resolvedConnectorsDeferred.complete(configuration.connectors)
                // server started
            }
        }
    }

    override fun start(wait: Boolean): ApplicationEngine {
        servers // start
        return this
    }

    override fun stop(gracePeriodMillis: Long, timeoutMillis: Long) {
        servers.forEach {
//            it.stop
        }
    }
}
