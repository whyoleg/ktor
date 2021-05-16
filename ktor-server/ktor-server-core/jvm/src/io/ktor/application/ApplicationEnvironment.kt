/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.application

import io.ktor.config.*
import io.ktor.util.*
import io.ktor.util.Logger
import org.slf4j.*
import kotlin.coroutines.*

/**
 * Represents an environment in which [Application] runs
 */
public actual interface ApplicationEnvironment {
    /**
     * Parent coroutine context for an application
     */
    public actual val parentCoroutineContext: CoroutineContext

    /**
     * [ClassLoader] used to load application.
     *
     * Useful for various reflection-based services, like dependency injection.
     */
    public val classLoader: ClassLoader get() = Application::class.java.classLoader

    /**
     * Instance of [Logger] to be used for logging.
     */
    public actual val log: Logger

    /**
     * Configuration for the [Application]
     */
    public actual val config: ApplicationConfig

    /**
     * Provides events on Application lifecycle
     */
    public actual val monitor: ApplicationEvents

    /**
     * Application's root path (prefix, context path in servlet container).
     */
    public actual val rootPath: String

    /**
     * Indicates if development mode is enabled.
     */
    public actual val developmentMode: Boolean
}
