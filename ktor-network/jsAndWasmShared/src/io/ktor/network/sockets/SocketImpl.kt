/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.network.sockets.nodejs.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.*
import io.ktor.network.sockets.nodejs.Socket as NodejsSocket

internal class ServerSocketImpl(
    override val socketContext: Job,
    private val incoming: ReceiveChannel<Socket>,
    private val server: Server
) : ServerSocket {
    override val localAddress: SocketAddress
        get() = checkNotNull(server.address()) { "Server is not yet bound" }.toSocketAddress()

    override suspend fun accept(): Socket = incoming.receive()

    init {
        socketContext.invokeOnCompletion {
            // TODO: is it enough?
            server.close()
        }
    }

    override fun close() {
        socketContext.cancel("Socket closed")
    }
}

internal class SocketImpl(
    override val localAddress: SocketAddress,
    override val remoteAddress: SocketAddress,
    override val coroutineContext: CoroutineContext,
    private val incoming: ReceiveChannel<JsBuffer>,
    private val socket: NodejsSocket
) : Socket {
    override val socketContext: Job get() = coroutineContext.job

    init {
        socketContext.invokeOnCompletion {
            socket.destroy()
            incoming.cancel(CancellationException("Socket closed", it))
        }
    }

    override fun attachForReading(channel: ByteChannel): WriterJob = writer(EmptyCoroutineContext, channel = channel) {
        incoming.consumeEach { buffer ->
            channel.writeByteArray(buffer.toByteArray())
        }
    }

    override fun attachForWriting(channel: ByteChannel): ReaderJob = reader(EmptyCoroutineContext, channel = channel) {
        while (true) {
            val result = channel.read { bytes, startIndex, endIndex ->
                socket.write(bytes.copyOfRange(startIndex, endIndex).toJsBuffer())
                endIndex - startIndex
            }
            if (result == -1) {
                socket.end()
                break
            }
        }
    }

    override fun close() {
        socketContext.cancel("Socket closed by user")
    }
}
