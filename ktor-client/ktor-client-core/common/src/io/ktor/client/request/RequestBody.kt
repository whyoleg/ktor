/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.request

import io.ktor.client.utils.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import kotlin.native.concurrent.*

@SharedImmutable
internal val BodyTypeAttributeKey: AttributeKey<TypeInfo> = AttributeKey("BodyTypeAttributeKey")

public inline fun <reified T> HttpRequestBuilder.setBody(body: T) {
    when (body) {
        null -> {
            this.body = EmptyContent
        }
        is String,
        is OutgoingContent,
        is ByteArray,
        is ByteReadChannel -> {
            this.body = body
        }
        else -> {
            this.body = body
            bodyType = typeInfo<T>()
        }
    }
}