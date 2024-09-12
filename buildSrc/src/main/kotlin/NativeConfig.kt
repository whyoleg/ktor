/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.*

fun Project.configureNative() {
    nativeTargets()

    // TODO: make it better
    tasks.findByName("linkDebugTestLinuxX64")?.onlyIf { HOST_NAME == "linux" }
    tasks.findByName("linkDebugTestLinuxArm64")?.onlyIf { HOST_NAME == "linux" }
    tasks.findByName("linkDebugTestMingwX64")?.onlyIf { HOST_NAME == "windows" }
}
