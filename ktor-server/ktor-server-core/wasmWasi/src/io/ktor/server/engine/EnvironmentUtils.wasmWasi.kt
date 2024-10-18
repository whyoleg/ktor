/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

internal actual fun getKtorEnvironmentProperties(): List<Pair<String, String>> = TODO()

internal actual fun getEnvironmentProperty(key: String): String? = TODO()

internal actual fun setEnvironmentProperty(key: String, value: String): Unit = TODO()

internal actual fun clearEnvironmentProperty(key: String): Unit = TODO()
