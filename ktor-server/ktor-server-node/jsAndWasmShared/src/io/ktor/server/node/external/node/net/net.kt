/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JsModule("net")

package io.ktor.server.node.external.node.net

import io.ktor.server.node.*

internal external interface Socket

internal external interface Server {
    fun listen(port: Int, listeningListener: () -> Unit): Server
    fun listen(port: Int, hostname: String, listeningListener: () -> Unit): Server
}
