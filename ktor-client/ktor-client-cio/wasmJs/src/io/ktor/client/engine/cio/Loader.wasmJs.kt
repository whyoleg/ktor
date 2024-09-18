/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.cio

import io.ktor.client.engine.*
import io.ktor.utils.io.*

@OptIn(InternalAPI::class, ExperimentalStdlibApi::class)
@Suppress("DEPRECATION")
@EagerInitialization
private val initHook: Unit = engines.append(CIO)
