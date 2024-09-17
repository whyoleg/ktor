/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JsModule("node:net")
@file:JsNonModule

package io.ktor.network.sockets.nodejs

internal external interface JsError // js.Error
internal external interface JsBuffer // Int8Array

internal external fun createConnection(options: CreateConnectionOptions): Socket

internal external interface CreateConnectionOptions {
    var timeout: Int?
    var allowHalfOpen: Boolean?
}

internal external interface TcpCreateConnectionOptions : CreateConnectionOptions {
    var port: Int
    var host: String?

    var localAddress: String?
    var localPort: Int?
    var family: Int? // ip stack
    var noDelay: Boolean?
    var keepAlive: Boolean?
}

internal external interface IpcCreateConnectionOptions : CreateConnectionOptions {
    var path: String
}

internal external interface Socket {
    val localAddress: String
    val localPort: Int

    val remoteAddress: String
    val remotePort: Int

    fun write(buffer: JsBuffer): Boolean

    fun destroy()
    fun destroy(error: JsError)

    // sends FIN
    fun end()

    fun on(event: String /* "close" */, listener: (hadError: Boolean) -> Unit)
    fun on(event: String /* "connect", "drain", "end", "timeout",  */, listener: () -> Unit)
    fun on(event: String /* "data" */, listener: (data: JsBuffer) -> Unit)
    fun on(event: String /* "error" */, listener: (error: JsError) -> Unit)
}

internal external fun createServer(options: CreateServerOptions): Server

internal external interface CreateServerOptions {
    var allowHalfOpen: Boolean?
    var keepAlive: Boolean?
    var noDelay: Boolean?
}

internal external interface Server {
    fun address(): ServerLocalAddressInfo?
    fun listen(options: ServerListenOptions)

    // stop accepting new connections
    fun close()

    fun on(event: String /* "close", "listening" */, listener: () -> Unit)
    fun on(event: String /* "connection" */, listener: (socket: Socket) -> Unit)
    fun on(event: String /* "error" */, listener: (error: JsError) -> Unit)
    fun on(event: String /* "drop" */, listener: (drop: ServerConnectionDrop) -> Unit)
}

internal external interface ServerLocalAddressInfo

internal external interface TcpServerLocalAddressInfo : ServerLocalAddressInfo {
    val address: String
    val family: String
    val port: Int
}

internal external interface ServerConnectionDrop

internal external interface ServerListenOptions {
    var port: Int?
    var host: String?
    var path: String?
}
