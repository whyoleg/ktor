/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("ktorbuild.multiplatform-all")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":ktor-utils"))
            }
        }
    }
}
