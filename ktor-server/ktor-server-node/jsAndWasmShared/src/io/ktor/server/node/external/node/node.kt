/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.node.external.node

import io.ktor.server.node.*
import io.ktor.server.node.external.node.http.*

internal external interface Readable

internal external interface Writable : WritableStream

internal external interface WritableStream {
    fun write(chunk: Uint8Array, callback: (error: JsError?) -> Unit): Boolean
    fun end(cb: () -> Unit)
    fun end(data: Uint8Array, cb: () -> Unit)
}

internal typealias RequestListener = (req: IncomingMessage, res: ServerResponse) -> Unit
