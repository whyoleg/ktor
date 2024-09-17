/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.network.selector.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

public fun SocketEngine(selector: SelectorManager): SocketEngine = SocketEngineImpl(selector)

public actual fun SocketEngine(): SocketEngine = SocketEngine(Dispatchers.IO)

public actual fun SocketEngine(context: CoroutineContext): SocketEngine = SocketEngine(SelectorManager(context))

private class SocketEngineImpl(private val selector: SelectorManager) : SocketEngine {
    override val coroutineContext: CoroutineContext get() = selector.coroutineContext

    override suspend fun tcpConnect(
        remoteAddress: SocketAddress,
        options: SocketOptions.TCPClientSocketOptions
    ): Socket = tcpConnect(selector, remoteAddress, options)

    override fun tcpBind(
        localAddress: SocketAddress?,
        options: SocketOptions.AcceptorOptions
    ): ServerSocket = tcpBind(selector, localAddress, options)

    override fun udpConnect(
        remoteAddress: SocketAddress,
        localAddress: SocketAddress?,
        options: SocketOptions.UDPSocketOptions
    ): ConnectedDatagramSocket = udpConnect(selector, remoteAddress, localAddress, options)

    override fun udpBind(
        localAddress: SocketAddress?,
        options: SocketOptions.UDPSocketOptions
    ): BoundDatagramSocket = udpBind(selector, localAddress, options)

    override fun close(): Unit = selector.close()
}

internal expect suspend fun tcpConnect(
    selector: SelectorManager,
    remoteAddress: SocketAddress,
    socketOptions: SocketOptions.TCPClientSocketOptions
): Socket

internal expect fun tcpBind(
    selector: SelectorManager,
    localAddress: SocketAddress?,
    socketOptions: SocketOptions.AcceptorOptions
): ServerSocket

internal expect fun udpConnect(
    selector: SelectorManager,
    remoteAddress: SocketAddress,
    localAddress: SocketAddress?,
    options: SocketOptions.UDPSocketOptions
): ConnectedDatagramSocket

internal expect fun udpBind(
    selector: SelectorManager,
    localAddress: SocketAddress?,
    options: SocketOptions.UDPSocketOptions
): BoundDatagramSocket
