/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.server.http

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.test.dispatcher.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlin.test.*

class ApplicationRequestContentTest {
    @Test
    fun testSimpleStringContent() = testSuspend {
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Call) {
                assertEquals("bodyContent", call.receiveText())
            }

            handleRequest(HttpMethod.Get, "") {
                setBody("bodyContent")
            }
        }
    }

    @Test
    fun testSimpleStringContentWithBadContentType() = testSuspend {
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Call) {
                assertFailsWith<BadRequestException> {
                    call.receiveText()
                }.let { throw it }
            }

            handleRequest(HttpMethod.Get, "") {
                addHeader(HttpHeaders.ContentType, "...la..la..la")
                setBody("any")
            }.let { call ->
                assertEquals(HttpStatusCode.BadRequest, call.response.status())
            }
        }
    }

    @Test
    fun testStringValues() = testSuspend {
        withTestApplication {
            val values = parametersOf("a", "1")

            application.intercept(ApplicationCallPipeline.Call) {
                val actual = call.receiveParameters()
                assertEquals(values, actual)
            }

            handleRequest(HttpMethod.Post, "") {
                addHeader(HttpHeaders.ContentType, "application/x-www-form-urlencoded")
                val value = values.formUrlEncode()
                setBody(value)
            }
        }
    }

    @Test
    fun testIllegalContentType() = testSuspend {
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Call) {
                assertFailsWith<BadRequestException> {
                    call.receiveParameters()
                }.let { throw it }
            }

            handleRequest(HttpMethod.Post, "") {
                addHeader(HttpHeaders.ContentType, "...la..la..la")
                setBody("don't care")
            }.let { call ->
                assertEquals(HttpStatusCode.BadRequest, call.response.status())
            }
        }
    }

    @Test
    fun testStringValuesWithCharset() = testSuspend {
        withTestApplication {
            val values = parametersOf("a", "1")

            application.intercept(ApplicationCallPipeline.Call) {
                assertEquals(values, call.receiveParameters())
            }

            handleRequest(HttpMethod.Post, "") {
                addHeader(HttpHeaders.ContentType, "application/x-www-form-urlencoded; charset=UTF-8")
                setBody(values.formUrlEncode())
            }
        }
    }

//    @Test
//    fun testInputStreamContent() {
//        withTestApplication {
//            application.intercept(ApplicationCallPipeline.Call) {
//                assertEquals("bodyContent", call.receiveStream().reader(Charsets.UTF_8).readText())
//            }
//
//            handleRequest(HttpMethod.Get, "") {
//                setBody("bodyContent")
//            }
//        }
//    }

    @Test
    fun testCustomTransform() = testSuspend {
        withTestApplication {
            val value = IntList(listOf(1, 2, 3, 4))

            application.receivePipeline.intercept(ApplicationReceivePipeline.Transform) { query ->
                if (query.typeInfo.type != IntList::class) return@intercept
                val message = query.value as? ByteReadChannel ?: return@intercept

                val string = message.readRemaining().readText()
                val transformed = IntList.parse(string)
                proceedWith(ApplicationReceiveRequest(query.typeInfo, transformed))
            }

            application.intercept(ApplicationCallPipeline.Call) {
                assertEquals(value, call.receive<IntList>())
            }

            handleRequest(HttpMethod.Get, "") {
                setBody(value.toString())
            }
        }
    }

    @Test
    fun testFormUrlEncodedContent() = testSuspend {
        val values = parametersOf(
            "one" to listOf("1"),
            "two_space_three_and_four" to listOf("2 3 & 4")
        )
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Call) {
                assertEquals(values, call.receiveParameters())
            }

            handleRequest(HttpMethod.Post, "") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
                setBody(values.formUrlEncode())
            }
        }
    }

    @Test
    fun testReceiveUnsupportedTypeFailing() = testSuspend {
        withTestApplication {
            application.install(ContentNegotiation)

            application.routing {
                get("/") {
                    val v = call.receive<IntList>()
                    call.respondText(v.values.joinToString())
                }
            }

            handleRequest(HttpMethod.Get, "/").let { call ->
                assertEquals(415, call.response.status()?.value)
            }
        }
    }

    @Test
    fun testReceiveUnsupportedTypeNotFailing() = testSuspend {
        withTestApplication {
            application.install(ContentNegotiation)

            application.routing {
                get("/") {
                    val v = call.receiveOrNull<IntList>()
                    call.respondText(v?.values?.joinToString() ?: "(none)")
                }
            }

            handleRequest(HttpMethod.Get, "/").let { call ->
                assertEquals(200, call.response.status()?.value)
            }
        }
    }

    @Test
    fun testDoubleReceiveWithNoFeature() = testSuspend {
        withTestApplication {
            application.intercept(ApplicationCallPipeline.Call) {
                assertEquals("bodyContent", call.receiveText())
                assertFailsWith<RequestAlreadyConsumedException> {
                    call.receiveText()
                }
            }

            handleRequest(HttpMethod.Get, "") {
                setBody("bodyContent")
            }
        }
    }

    @Test
    fun testDoubleReceive() = testSuspend {
        withTestApplication {
            application.install(DoubleReceive)

            application.intercept(ApplicationCallPipeline.Call) {
                assertEquals("bodyContent", call.receiveText())
                assertEquals("bodyContent", call.receiveText())
                assertFailsWith<RequestAlreadyConsumedException> {
                    // we can't receive a stream because we have a string cached
                    call.receiveChannel()
                    //                call.receiveStream()
                }
            }

            handleRequest(HttpMethod.Get, "") {
                setBody("bodyContent")
            }
        }
    }

    @Test
    fun testDoubleReceiveDifferentTypes() = testSuspend {
        withTestApplication {
            application.install(DoubleReceive)

            application.intercept(ApplicationCallPipeline.Call) {
                assertEquals("bodyContent".toByteArray().toList(), call.receive<ByteArray>().toList())
                assertEquals("bodyContent", call.receiveText())

                // this also works because we already have a byte array cached
                //            assertEquals("bodyContent", call.receiveStream().reader().readText())
                assertEquals("bodyContent", call.receiveChannel().readUTF8Line())
            }

            handleRequest(HttpMethod.Get, "") {
                setBody("bodyContent")
            }
        }
    }

//    @Test
//    fun testDoubleReceiveStreams(): Unit = withTestApplication {
//        application.install(DoubleReceive)
//
//        application.intercept(ApplicationCallPipeline.Call) {
//            assertEquals(11, call.receiveStream().readBytes().size)
//            assertFailsWith<RequestAlreadyConsumedException> {
//                // a stream can't be received twice
//                call.receiveStream()
//            }
//        }
//
//        handleRequest(HttpMethod.Get, "") {
//            setBody("bodyContent")
//        }
//    }

    @Test
    fun testDoubleReceiveChannels() = testSuspend {
        withTestApplication {
            application.install(DoubleReceive)

            application.intercept(ApplicationCallPipeline.Call) {
                call.receiveChannel().readRemaining().use { packet ->
                    assertEquals(11, packet.remaining)
                }
                assertFailsWith<RequestAlreadyConsumedException> {
                    // a channel can't be received twice
                    call.receiveChannel()
                }
            }

            handleRequest(HttpMethod.Get, "") {
                setBody("bodyContent")
            }
        }
    }

    @Test
    fun testDoubleReceiveAfterTransformationFailed() = testSuspend {
        withTestApplication {
            application.install(DoubleReceive)

            application.receivePipeline.intercept(ApplicationReceivePipeline.Transform) {
                if (it.typeInfo.type == IntList::class) {
                    throw MySpecialException()
                }
            }
            application.intercept(ApplicationCallPipeline.Call) {
                assertFailsWith<MySpecialException> {
                    call.receive<IntList>()
                }
                val cause = assertFailsWith<RequestReceiveAlreadyFailedException> {
                    call.receive<IntList>()
                }
                assertTrue { cause.cause is MySpecialException }
            }

            handleRequest(HttpMethod.Get, "") {
                setBody("bodyContent")
            }
        }
    }
}

data class IntList(val values: List<Int>) {
    override fun toString() = "$values"

    companion object {
        fun parse(text: String) = IntList(text.removeSurrounding("[", "]").split(",").map { it.trim().toInt() })
    }
}

private class MySpecialException : Exception("Expected exception")
