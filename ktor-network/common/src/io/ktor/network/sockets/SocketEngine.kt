/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

public expect fun SocketEngine(): SocketEngine
public expect fun SocketEngine(context: CoroutineContext): SocketEngine

// TODO[whyoleg] update docs
public interface SocketEngine : CoroutineScope, Closeable {
    public suspend fun tcpConnect(
        remoteAddress: SocketAddress,
        options: SocketOptions.TCPClientSocketOptions
    ): Socket

    public fun tcpBind(
        localAddress: SocketAddress?,
        options: SocketOptions.AcceptorOptions
    ): ServerSocket

    public fun udpConnect(
        remoteAddress: SocketAddress,
        localAddress: SocketAddress?,
        options: SocketOptions.UDPSocketOptions
    ): ConnectedDatagramSocket

    public fun udpBind(
        localAddress: SocketAddress?,
        options: SocketOptions.UDPSocketOptions
    ): BoundDatagramSocket
}
