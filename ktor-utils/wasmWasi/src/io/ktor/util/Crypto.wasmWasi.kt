/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.util

/**
 * Generates a nonce string.
 */
public actual fun generateNonce(): String {
    TODO("Not supported yet")
}

/**
 * Create [Digest] from specified hash [name].
 */
public actual fun Digest(name: String): Digest = error("[Digest] is not supported on WasmWasi")
