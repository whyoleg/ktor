/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.utils.io.bits.*

public actual suspend inline fun <T> ByteReadChannel.readExact(
    size: Int,
    block: (memory: Memory, startIndex: Int, endIndex: Int) -> T
): T {
}
