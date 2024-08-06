/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.node

import io.ktor.server.testing.suites.*

class NodeServerTest : HttpServerCommonTestSuite<NodeApplicationEngine, NodeApplicationEngine.Configuration>(Node)
