/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.node

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.node.external.node.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.*

internal class NodeApplicationCall(
    application: Application,
    private val nodeRequest: IncomingMessage,
    private val nodeResponse: ServerResponse,
    private val output: ByteWriteChannel
) : BaseApplicationCall(application) {
    override val request: BaseApplicationRequest = Request()
    override val response: BaseApplicationResponse = Response()

    inner class Request : BaseApplicationRequest(this@NodeApplicationCall) {
        override val engineHeaders: Headers
            get() = TODO("Not yet implemented")
        override val engineReceiveChannel: ByteReadChannel
            get() = TODO("Not yet implemented")
        override val local: RequestConnectionPoint
            get() = TODO("Not yet implemented")
        override val queryParameters: Parameters
            get() = TODO("Not yet implemented")
        override val rawQueryParameters: Parameters
            get() = TODO("Not yet implemented")
        override val cookies: RequestCookies
            get() = TODO("Not yet implemented")
    }

    inner class Response : BaseApplicationResponse(this@NodeApplicationCall) {
        override val headers: ResponseHeaders = object : ResponseHeaders() {
            override fun engineAppendHeader(name: String, value: String) {
                nodeResponse.setHeader(name, value)
            }

            override fun getEngineHeaderNames(): List<String> {
                return nodeResponse.getHeaderNames().toList()
            }

            override fun getEngineHeaderValues(name: String): List<String> {
                return nodeResponse.getHeader(name).toList()
            }
        }

        override fun setStatus(statusCode: HttpStatusCode) {
            nodeResponse.statusCode = statusCode.value
        }

        override suspend fun responseChannel(): ByteWriteChannel = output

        override suspend fun respondUpgrade(upgrade: OutgoingContent.ProtocolUpgrade) {
            TODO("Not yet implemented")
        }
    }

}
