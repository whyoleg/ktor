/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("CoroutineDispatcherUtilsKt")

package io.ktor.client.utils

import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlin.jvm.*

/**
 * Creates [CoroutineDispatcher] for client with fixed [threadCount] and specified [dispatcherName].
 */
@InternalAPI
public actual fun Dispatchers.clientDispatcher(
    threadCount: Int,
    dispatcherName: String
): CoroutineDispatcher = Dispatchers.IO.limitedParallelism(threadCount, dispatcherName)
