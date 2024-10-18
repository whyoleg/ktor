/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import internal.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.*

fun Project.configureWasmJs() {
    kotlin {
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            nodejs { useMochaForTests() }
            if (project.targetIsEnabled("wasmJs.browser")) browser { useKarmaForTests() }
        }

        sourceSets {
            wasmJsMain {
                dependencies {
                    implementation(libs.kotlinx.browser)
                }
            }
            wasmJsTest {
                dependencies {
                    implementation(npm("puppeteer", libs.versions.puppeteer.get()))
                }
            }
        }
    }

    configureJsTestTasks(target = "wasmJs")
}

fun Project.configureWasmWasi() {
    if (!project.targetIsEnabled("wasmWasi")) return

    kotlin {
        @OptIn(ExperimentalWasmDsl::class)
        wasmWasi {
            nodejs()
        }
    }
}
