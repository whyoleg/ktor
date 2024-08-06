/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

description = ""

kotlin.sourceSets {
    jsAndWasmSharedMain {
        dependencies {
            api(project(":ktor-server:ktor-server-core"))
        }
    }
    jsAndWasmSharedTest {
        dependencies {
            api(project(":ktor-server:ktor-server-test-suites"))
        }
    }
}
