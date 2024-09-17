/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets.tests

import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlin.random.*
import kotlin.test.*

class UDPSocketTest {

    private val done = atomic(0)

    @Test
    fun testBroadcastFails() = testSockets { builder ->
        if (isJvmWindows()) {
            return@testSockets
        }

        lateinit var socket: BoundDatagramSocket
        var denied = false
        try {
            socket = builder.udp().bind()

            socket.use {
                val datagram = Datagram(
                    packet = buildPacket { writeText("0123456789") },
                    address = InetSocketAddress("255.255.255.255", 56700)
                )

                it.send(datagram)
            }
        } catch (cause: Exception) {
            when {
                // Java
                cause.message?.contains("Permission denied", ignoreCase = true) == true -> {
                    denied = true
                }
                // PosixException (WSAEACCES)
                cause.message?.contains("10013", ignoreCase = true) == true -> {
                    denied = true
                }

                else                                                        -> {
                    throw cause
                }
            }
        }

        assertTrue(denied)
        socket.socketContext.join()
        assertTrue(socket.isClosed)
    }

    @Test
    fun testBroadcastSuccessful() = testSockets { builder ->
        val serverSocketCompletable = CompletableDeferred<BoundDatagramSocket>()
        val server = launch {
            builder.udp().bind(InetSocketAddress("0.0.0.0", 0)).use { socket ->
                serverSocketCompletable.complete(socket)
                val received = socket.receive()
                assertEquals("0123456789", received.packet.readText())
            }
        }

        val serverSocket = serverSocketCompletable.await()

        val clientSocket = builder.udp().bind {
            broadcast = true
        }
        clientSocket.use { socket ->
            val inetSocketAddress = serverSocket.localAddress as? InetSocketAddress
                ?: error("Should be an inet address")

            socket.send(
                Datagram(
                    packet = buildPacket { writeText("0123456789") },
                    address = InetSocketAddress("255.255.255.255", inetSocketAddress.port)
                )
            )
        }

        server.join()

        serverSocket.socketContext.join()
        assertTrue(serverSocket.isClosed)

        clientSocket.socketContext.join()
        assertTrue(clientSocket.isClosed)
    }

    @Test
    fun testClose() = testSockets { builder ->
        val socket = builder.udp().bind()

        socket.close()

        socket.socketContext.join()
        assertTrue(socket.isClosed)
    }

    @Test
    fun testInvokeOnClose() = testSockets { builder ->
        val socket: BoundDatagramSocket = builder.udp().bind()

        socket.outgoing.invokeOnClose {
            done.addAndGet(1)
        }

        assertFailsWith<IllegalStateException> {
            socket.outgoing.invokeOnClose {
                done.addAndGet(2)
            }
        }

        socket.close()
        socket.close()

        socket.socketContext.join()
        assertTrue(socket.isClosed)
        assertEquals(1, done.value)
    }

    @Test
    fun testOutgoingInvokeOnClose() = testSockets { builder ->
        val socket: BoundDatagramSocket = builder.udp().bind()

        socket.outgoing.invokeOnClose {
            done.addAndGet(1)
            assertTrue(it is AssertionError)
        }

        socket.outgoing.close(AssertionError())

        assertEquals(1, done.value)
        socket.socketContext.join()
        assertTrue(socket.isClosed)
    }

    @Test
    fun testOutgoingInvokeOnCloseWithSocketClose() = testSockets { builder ->
        val socket: BoundDatagramSocket = builder.udp().bind()

        socket.outgoing.invokeOnClose {
            done.addAndGet(1)
        }

        socket.close()

        assertEquals(1, done.value)

        socket.socketContext.join()
        assertTrue(socket.isClosed)
    }

    @Test
    fun testOutgoingInvokeOnClosed() = testSockets { builder ->
        val socket: BoundDatagramSocket = builder.udp().bind()

        socket.outgoing.close(AssertionError())

        socket.outgoing.invokeOnClose {
            done.addAndGet(1)
            assertTrue(it is AssertionError)
        }

        assertEquals(1, done.value)

        socket.socketContext.join()
        assertTrue(socket.isClosed)
    }

    @Test
    fun testSendReceive() = testSockets { builder ->
        builder.udp().bind(InetSocketAddress("127.0.0.1", 8000)) {
            reuseAddress = true
        }.use { socket ->
            val localAddress = socket.localAddress as? InetSocketAddress
            assertEquals(8000, localAddress?.port)

            // Send messages to localhost
            launch {
                val address = InetSocketAddress("127.0.0.1", 8000)
                repeat(10) {
                    val bytePacket = buildPacket { append("hello") }
                    val data = Datagram(bytePacket, address)
                    socket.send(data)
                    assertTrue(data.packet.exhausted())
                }
            }

            // Receive messages from localhost
            repeat(10) {
                val incoming = socket.receive()
                assertEquals("hello", incoming.packet.readText())
            }
        }
    }

    @Test
    fun testSendReceiveLarge() = testSockets { builder ->
        val datagramSize = 10000 // must be larger than Segment.SIZE (8192) for this test
        val largeData = Random.nextBytes(datagramSize)

        builder.udp().bind(InetSocketAddress("127.0.0.1", 0)) {
            reuseAddress = true
            sendBufferSize = 65535
            receiveBufferSize = 65535
        }.use { socket ->
            // Send messages to localhost
            launch {
                repeat(4) {
                    val bytePacket = buildPacket { write(largeData) }
                    val data = Datagram(bytePacket, socket.localAddress)
                    socket.send(data)
                    assertTrue(data.packet.exhausted())
                }
            }

            // Receive messages from localhost
            repeat(4) {
                val incoming = socket.receive()
                assertContentEquals(largeData, incoming.packet.readByteArray())
            }
        }
    }

    @Test
    fun testUdpConnect() = testSockets { builder ->
        val server = builder.udp().bind()

        val remoteAddress = InetSocketAddress("127.0.0.1", (server.localAddress as InetSocketAddress).port)
        val socket = builder.udp().connect(remoteAddress)

        socket.send(Datagram(buildPacket { writeText("hello") }, remoteAddress))
        assertEquals("hello", server.receive().packet.readText())
    }
}

expect fun isJvmWindows(): Boolean
