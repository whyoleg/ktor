/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.cio

import io.ktor.network.sockets.*
import io.ktor.test.dispatcher.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlin.test.*

class ConnectionFactoryTest {

    private lateinit var socketEngine: SocketEngine

    @BeforeTest
    fun setup() {
        socketEngine = SocketEngine()
    }

    @AfterTest
    fun teardown() {
        socketEngine.close()
    }

    @Test
    fun testLimitSemaphore() = runTestWithRealTime {
        val connectionFactory = ConnectionFactory(
            socketEngine,
            connectionsLimit = 2,
            addressConnectionsLimit = 1,
        )
        withServerSocket { socket0 ->
            withServerSocket { socket1 ->
                withServerSocket { socket2 ->
                    connectionFactory.connect(socket0.localAddress as InetSocketAddress)
                    connectionFactory.connect(socket1.localAddress as InetSocketAddress)

                    assertTimeout {
                        connectionFactory.connect(socket2.localAddress as InetSocketAddress)
                    }
                }
            }
        }
    }

    @Test
    fun testAddressSemaphore() = runTestWithRealTime {
        val connectionFactory = ConnectionFactory(
            socketEngine,
            connectionsLimit = 2,
            addressConnectionsLimit = 1,
        )
        withServerSocket { socket0 ->

            withServerSocket { socket1 ->
                connectionFactory.connect(socket0.localAddress as InetSocketAddress)
                assertTimeout {
                    connectionFactory.connect(socket0.localAddress as InetSocketAddress)
                }

                connectionFactory.connect(socket1.localAddress as InetSocketAddress)
                assertTimeout {
                    connectionFactory.connect(socket1.localAddress as InetSocketAddress)
                }
            }
        }
    }

    @Test
    fun testReleaseLimitSemaphoreWhenFailed() = runTestWithRealTime {
        val connectionFactory = ConnectionFactory(
            socketEngine,
            connectionsLimit = 2,
            addressConnectionsLimit = 1,
        )
        withServerSocket { socket0 ->
            withServerSocket { socket1 ->
                connectionFactory.connect(socket0.localAddress as InetSocketAddress)

                // Release the `limit` semaphore when it fails to acquire the address semaphore.
                assertTimeout {
                    connectionFactory.connect(socket0.localAddress as InetSocketAddress)
                }

                connectionFactory.connect(socket1.localAddress as InetSocketAddress)
            }
        }
    }

    private suspend fun assertTimeout(timeoutMillis: Long = 500, block: suspend () -> Unit) {
        assertFailsWith(TimeoutCancellationException::class) {
            withTimeout(timeoutMillis) {
                block()
            }
        }
    }

    private suspend fun withServerSocket(block: suspend (ServerSocket) -> Unit) {
        SocketBuilder(socketEngine).tcp().bind(TEST_SERVER_SOCKET_HOST, 0).use { socket ->
            delay(500) // await server start on nodes
            block(socket)
        }
    }

    companion object {
        private const val TEST_SERVER_SOCKET_HOST = "127.0.0.1"
    }
}
