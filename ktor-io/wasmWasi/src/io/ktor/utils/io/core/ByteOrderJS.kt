/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

// ktlint-disable filename
package io.ktor.utils.io.core

public actual enum class ByteOrder {
    BIG_ENDIAN, LITTLE_ENDIAN;

    public actual companion object {
        // TODO
        public actual fun nativeOrder(): ByteOrder = ByteOrder.BIG_ENDIAN
    }
}
