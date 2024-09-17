/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package io.ktor.network.sockets

import io.ktor.network.selector.*

/**
 * Start building a socket
 */
public fun aSocket(selector: SelectorManager): SocketBuilder = SocketBuilder(SocketEngine(selector))

/**
 * Set TCP_NODELAY socket option to disable the Nagle algorithm.
 */
@Deprecated("noDelay is true by default")
public fun <T : Configurable<T, *>> T.tcpNoDelay(): T {
    return configure {
        if (this is SocketOptions.TCPClientSocketOptions) {
            noDelay = true
        }
    }
}
