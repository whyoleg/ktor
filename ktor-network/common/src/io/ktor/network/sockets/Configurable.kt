/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

/**
 * Represent a configurable socket builder
 */
public interface Configurable<out T : Configurable<T, Options>, Options : SocketOptions> {
    /**
     * Current socket options
     */
    public var options: Options

    /**
     * Configure socket options in [block] function
     */
    public fun configure(block: Options.() -> Unit): T {
        @Suppress("UNCHECKED_CAST")
        val newOptions = options.copy() as Options

        block(newOptions)
        options = newOptions

        @Suppress("UNCHECKED_CAST")
        return this as T
    }
}
