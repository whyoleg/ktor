/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets.tests

import io.ktor.network.sockets.*
import io.ktor.test.dispatcher.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.io.files.*
import kotlin.time.*
import kotlin.time.Duration.Companion.minutes

internal fun testSockets(
    timeout: Duration = 1.minutes,
    block: suspend CoroutineScope.(builder: SocketBuilder) -> Unit
): TestResult = runTestWithRealTime(timeout = timeout) {
    SocketEngine().use {
        block(SocketBuilder(it))
    }
}

internal expect fun Any.supportsUnixDomainSockets(): Boolean

internal fun createTempFilePath(basename: String): String {
    return Path(SystemTemporaryDirectory, basename).toString()
}

internal fun removeFile(path: String) {
    SystemFileSystem.delete(Path(path), mustExist = false)
}
