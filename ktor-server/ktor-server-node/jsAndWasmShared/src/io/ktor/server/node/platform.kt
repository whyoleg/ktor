/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.node

@Target(AnnotationTarget.FILE)
internal expect annotation class JsModule(val import: String)

/* string[] */
internal external class JsStringArray

/* number | string | string[] | undefined */
internal external class JsHeaderArray

internal external class Uint8Array

internal external class JsError

internal expect fun JsStringArray.toList(): List<String>
internal expect fun JsHeaderArray.toList(): List<String>
internal expect fun Uint8Array.toByteArray(): ByteArray
internal expect fun ByteArray.toUint8Array(): Uint8Array
