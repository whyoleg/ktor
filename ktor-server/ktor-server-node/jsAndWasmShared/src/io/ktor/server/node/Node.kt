/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.node

import io.ktor.events.*
import io.ktor.server.application.*
import io.ktor.server.engine.*

public object Node : ApplicationEngineFactory<NodeApplicationEngine, NodeApplicationEngine.Configuration> {

    override fun configuration(
        configure: NodeApplicationEngine.Configuration.() -> Unit
    ): NodeApplicationEngine.Configuration {
        return NodeApplicationEngine.Configuration().apply(configure)
    }

    override fun create(
        environment: ApplicationEnvironment,
        monitor: Events,
        developmentMode: Boolean,
        configuration: NodeApplicationEngine.Configuration,
        applicationProvider: () -> Application
    ): NodeApplicationEngine {
        return NodeApplicationEngine(environment, monitor, developmentMode, configuration, applicationProvider)
    }
}

