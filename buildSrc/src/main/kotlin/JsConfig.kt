/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
@file:Suppress("UNUSED_VARIABLE")

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import java.io.*

fun Project.configureJs() {
    configureJsTasks()

    kotlin {
        sourceSets {
            val jsTest by getting {
                dependencies {
                    implementation(npm("puppeteer", Versions.puppeteer))
                }
            }
        }
    }

    configureJsTestTasks()
}

private fun Project.configureJsTasks() {
    kotlin {
        js {
            nodejs {
                testTask {
                    useMocha {
                        timeout = "10000"
                    }
                }
            }

            // we don't test `server` modules in a browser.
            // there are 2 explanations why:
            // * logical - we don't need server in browser
            // * technical - we don't have access to files, os, etc.
            // Also, because of the ` origin ` URL in a browser, special support in `test-host` need to be implemented
            if (!project.name.startsWith("ktor-server")) browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                        useConfigDirectory(File(project.rootProject.projectDir, "karma"))
                    }
                }
            }

            binaries.library()
        }
    }
}

private fun Project.configureJsTestTasks() {
    val shouldRunJsBrowserTest = !hasProperty("teamcity") || hasProperty("enable-js-tests")
    if (shouldRunJsBrowserTest) return

    tasks.findByName("cleanJsBrowserTest")?.onlyIf { false }
    tasks.findByName("jsBrowserTest")?.onlyIf { false }
}
