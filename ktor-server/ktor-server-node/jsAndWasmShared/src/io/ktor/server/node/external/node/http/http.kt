/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JsModule("http")

package io.ktor.server.node.external.node.http

import io.ktor.server.node.*
import io.ktor.server.node.external.node.*
import io.ktor.server.node.external.node.net.*
import io.ktor.server.node.external.node.net.Server as NetServer

internal external interface Server : NetServer

internal external interface IncomingMessage : Readable {
    var aborted: Boolean
    var httpVersion: String
    var httpVersionMajor: Int
    var httpVersionMinor: Int
    var complete: Boolean
    var connection: Socket
    var socket: Socket
    var rawHeaders: JsStringArray
    var rawTrailers: JsStringArray
    fun setTimeout(msecs: Int): IncomingMessage
    fun setTimeout(msecs: Int, callback: () -> Unit): IncomingMessage
    var method: String
    var url: String
    var statusCode: Int
    var statusMessage: String
}

internal external interface OutgoingMessage : Writable {
    var upgrading: Boolean
    var chunkedEncoding: Boolean
    var shouldKeepAlive: Boolean
    var useChunkedEncodingByDefault: Boolean
    var sendDate: Boolean
    var finished: Boolean
    var headersSent: Boolean
    var connection: Socket
    var socket: Socket
    fun setTimeout(msecs: Int): OutgoingMessage
    fun setTimeout(msecs: Int, callback: () -> Unit): OutgoingMessage
    fun setHeader(name: String, value: Int)
    fun setHeader(name: String, value: String)
    fun setHeader(name: String, value: JsStringArray)
    fun getHeader(name: String): JsHeaderArray
    fun getHeaderNames(): JsStringArray
    fun hasHeader(name: String): Boolean
    fun removeHeader(name: String)
    fun flushHeaders()
}

internal external interface ServerResponse : OutgoingMessage {
    var statusCode: Int
}


internal external fun createServer(requestListener: RequestListener): Server
