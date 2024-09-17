/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets.nodejs

import io.ktor.network.sockets.*

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.FILE)
internal expect annotation class JsNonModule()

@Target(AnnotationTarget.FILE)
internal expect annotation class JsModule(val import: String)

internal expect fun TcpCreateConnectionOptions(block: TcpCreateConnectionOptions.() -> Unit): TcpCreateConnectionOptions
internal expect fun IpcCreateConnectionOptions(block: IpcCreateConnectionOptions.() -> Unit): IpcCreateConnectionOptions

internal expect fun CreateServerOptions(block: CreateServerOptions.() -> Unit): CreateServerOptions

internal expect fun ServerListenOptions(block: ServerListenOptions.() -> Unit): ServerListenOptions

internal expect fun JsError.toThrowable(): Throwable

internal expect fun ByteArray.toJsBuffer(): JsBuffer
internal expect fun JsBuffer.toByteArray(): ByteArray

internal expect fun ServerLocalAddressInfo.toSocketAddress(): SocketAddress
