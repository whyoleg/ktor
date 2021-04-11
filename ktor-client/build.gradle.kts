/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

kotlin.sourceSets {
    val commonMain by getting {
        repositories {
            maven ( "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev" )
        }
        dependencies {
            api(project(":ktor-client:ktor-client-core"))
        }
    }
}
