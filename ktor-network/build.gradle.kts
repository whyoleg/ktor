/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

description = "Ktor network utilities"

kotlin {
    createCInterop("network", nixTargets()) {
        definitionFile = projectDir.resolve("nix/interop/network.def")
    }

    createCInterop("network", androidNativeTargets()) {
        definitionFile = projectDir.resolve("androidNative/interop/network.def")
    }

    sourceSets {
        jvmAndPosixMain {
            dependencies {
                api(project(":ktor-utils"))
            }
        }

        jvmAndPosixTest {
            dependencies {
                api(project(":ktor-test-dispatcher"))
            }
        }

        jvmTest {
            dependencies {
                implementation(project(":ktor-shared:ktor-junit"))
                implementation(libs.mockk)
            }
        }

        macosTest {
            val nixTest = getByName("posixTest")
            dependsOn(nixTest)
        }
        watchosTest {
            val nixTest = getByName("posixTest")
            dependsOn(nixTest)
        }
        tvosTest {
            val nixTest = getByName("posixTest")
            dependsOn(nixTest)
        }
        iosTest {
            val nixTest = getByName("posixTest")
            dependsOn(nixTest)
        }
    }
}
