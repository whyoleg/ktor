/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.network.sockets.nodejs.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.io.*
import kotlin.coroutines.*
import io.ktor.network.sockets.nodejs.Socket as NodejsSocket

public actual fun SocketEngine(): SocketEngine = SocketEngine(EmptyCoroutineContext)
public actual fun SocketEngine(context: CoroutineContext): SocketEngine =
    SocketEngineImpl(context + SupervisorJob(context[Job]))

private class SocketEngineImpl(
    override val coroutineContext: CoroutineContext
) : SocketEngine {
    override suspend fun tcpConnect(
        remoteAddress: SocketAddress,
        options: SocketOptions.TCPClientSocketOptions
    ): Socket = suspendCancellableCoroutine { cont ->
        val socket = createConnection(
            when (remoteAddress) {
                is InetSocketAddress -> TcpCreateConnectionOptions {
                    host = remoteAddress.hostname
                    port = remoteAddress.port
                    noDelay = options.noDelay
                    timeout = when (options.socketTimeout) {
                        Long.MAX_VALUE -> Int.MAX_VALUE
                        else           -> options.socketTimeout.toInt()
                    }
                    keepAlive = options.keepAlive
                }

                is UnixSocketAddress -> IpcCreateConnectionOptions {
                    path = remoteAddress.path
                    timeout = when (options.socketTimeout) {
                        Long.MAX_VALUE -> Int.MAX_VALUE
                        else           -> options.socketTimeout.toInt()
                    }
                }
            }
        )
        val incoming = Channel<JsBuffer>(Channel.UNLIMITED)
        val context = coroutineContext + Job(coroutineContext.job)
        socket.on("connect", fun() {
            // TODO unix address
            cont.resume(
                SocketImpl(
                    localAddress = when (remoteAddress) {
                        is UnixSocketAddress -> remoteAddress
                        else                 -> InetSocketAddress(socket.localAddress, socket.localPort)
                    },

                    remoteAddress = when (remoteAddress) {
                        is UnixSocketAddress -> remoteAddress
                        else                 -> InetSocketAddress(socket.remoteAddress, socket.remotePort)
                    },
                    coroutineContext = context,
                    incoming = incoming,
                    socket = socket
                )
            )
        })
        socket.on("error", fun(error: JsError) {
            if (cont.isActive) cont.resumeWithException(IOException("Failed to connect", error.toThrowable()))
            else context.job.cancel("Socket error", error.toThrowable())
        })
        socket.on("timeout", fun() {
            if (cont.isActive) cont.resumeWithException(SocketTimeoutException("timeout"))
            else context.job.cancel("Socket timeout", SocketTimeoutException("timeout"))
        })
        socket.on("end", fun() {
            incoming.close()
        })
        socket.on("close", fun(_: Boolean) {
            context.job.cancel("Socket closed")
        })
        socket.on("data", fun(data: JsBuffer) {
            incoming.trySend(data)
        })
    }

    override fun tcpBind(localAddress: SocketAddress?, options: SocketOptions.AcceptorOptions): ServerSocket {
        val server = createServer(CreateServerOptions {})
        val incomingSockets = Channel<Socket>(Channel.UNLIMITED)
        val serverContext = coroutineContext + SupervisorJob(coroutineContext.job)
        server.on("connection", fun(socket: NodejsSocket) {
            val incoming = Channel<JsBuffer>(Channel.UNLIMITED)
            val context = serverContext + Job(serverContext.job)
            socket.on("error", fun(error: JsError) {
                context.job.cancel("Socket error", error.toThrowable())
            })
            socket.on("timeout", fun() {
                context.job.cancel("Socket timeout", SocketTimeoutException("timeout"))
            })
            socket.on("end", fun() {
                incoming.close()
            })
            socket.on("close", fun(_: Boolean) {
                context.job.cancel("Socket closed")
            })
            socket.on("data", fun(data: JsBuffer) {
                incoming.trySend(data)
            })

            incomingSockets.trySend(
                // TODO unix address
                SocketImpl(
                    localAddress = when (localAddress) {
                        is UnixSocketAddress -> localAddress
                        else                 -> InetSocketAddress(socket.localAddress, socket.localPort)
                    },

                    remoteAddress = when (localAddress) {
                        is UnixSocketAddress -> localAddress
                        else                 -> InetSocketAddress(socket.remoteAddress, socket.remotePort)
                    },
                    coroutineContext = context,
                    incoming = incoming,
                    socket = socket
                )
            )
        })
        server.on("close", fun() {
            serverContext.job.cancel("Server closed")
        })
        server.on("error", fun(error: JsError) {
            serverContext.job.cancel("Server failed", error.toThrowable())
        })
        server.on("drop", fun(_: ServerConnectionDrop) {
            // TODO?
        })
        server.listen(ServerListenOptions {
            when (localAddress) {
                is InetSocketAddress -> {
                    port = localAddress.port
                    host = localAddress.hostname
                }

                is UnixSocketAddress -> {
                    path = localAddress.path
                }

                null                 -> {
                    host = "localhost"
                    port = 0
                }
            }
        })
        return ServerSocketImpl(serverContext.job, incomingSockets, server)
    }

    override fun udpConnect(
        remoteAddress: SocketAddress,
        localAddress: SocketAddress?,
        options: SocketOptions.UDPSocketOptions
    ): ConnectedDatagramSocket {
        TODO("Not yet implemented")
    }

    override fun udpBind(localAddress: SocketAddress?, options: SocketOptions.UDPSocketOptions): BoundDatagramSocket {
        TODO("Not yet implemented")
    }

    override fun close() {
        coroutineContext.job.cancel("Socket engine closed")
    }
}

