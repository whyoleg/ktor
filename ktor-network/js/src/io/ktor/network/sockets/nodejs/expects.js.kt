/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets.nodejs

import io.ktor.network.sockets.*
import org.khronos.webgl.*

@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias JsNonModule = kotlin.js.JsNonModule

@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias JsModule = kotlin.js.JsModule

internal actual fun TcpCreateConnectionOptions(block: TcpCreateConnectionOptions.() -> Unit): TcpCreateConnectionOptions =
    createObject(block)

internal actual fun IpcCreateConnectionOptions(block: IpcCreateConnectionOptions.() -> Unit): IpcCreateConnectionOptions =
    createObject(block)

internal actual fun CreateServerOptions(block: CreateServerOptions.() -> Unit): CreateServerOptions =
    createObject(block)

internal actual fun ServerListenOptions(block: ServerListenOptions.() -> Unit): ServerListenOptions =
    createObject(block)

private fun <T> createObject(block: T.() -> Unit): T = js("{}").unsafeCast<T>().apply(block)

internal actual fun JsError.toThrowable(): Throwable = unsafeCast<Throwable>()

internal actual fun ByteArray.toJsBuffer(): JsBuffer = unsafeCast<JsBuffer>()
internal actual fun JsBuffer.toByteArray(): ByteArray =
    Int8Array(this.unsafeCast<ArrayBuffer>()).unsafeCast<ByteArray>()

internal actual fun ServerLocalAddressInfo.toSocketAddress(): SocketAddress {
    if (jsTypeOf(this) == "string") return UnixSocketAddress(unsafeCast<String>())
    val info = unsafeCast<TcpServerLocalAddressInfo>()
    return InetSocketAddress(info.address, info.port)
}
