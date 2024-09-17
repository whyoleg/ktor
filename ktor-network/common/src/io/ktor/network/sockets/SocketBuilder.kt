/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

/**
 * Socket builder
 */
public class SocketBuilder internal constructor(
    private val engine: SocketEngine,
    override var options: SocketOptions
) : Configurable<SocketBuilder, SocketOptions> {
    public constructor(engine: SocketEngine) : this(engine, SocketOptions.create())

    /**
     * Build TCP socket.
     */
    public fun tcp(): TcpSocketBuilder = TcpSocketBuilder(engine, options.peer())

    /**
     * Build UDP socket.
     */
    public fun udp(): UDPSocketBuilder = UDPSocketBuilder(engine, options.peer().udp())
}
