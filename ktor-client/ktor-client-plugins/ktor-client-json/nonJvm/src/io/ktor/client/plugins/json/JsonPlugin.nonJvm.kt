/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION_ERROR")

package io.ktor.client.plugins.json

import io.ktor.utils.io.*
import kotlin.reflect.*

/**
 * Platform default serializer.
 */
@OptIn(InternalAPI::class)
public actual fun defaultSerializer(): JsonSerializer = serializers.first()

@InternalAPI
public val serializers: MutableList<JsonSerializer> = mutableListOf()

@InternalAPI
public val serializersStore: MutableList<JsonSerializer> get() = serializers

internal actual val DefaultIgnoredTypes: Set<KClass<*>> = setOf()
