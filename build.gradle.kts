// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsKotlinJvm) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

// Integration Testing Tasks
tasks.register("buildIntegrationTestApks") {
    group = "verification"
    description = "Build debug APKs for both Narada and Mahati"

    dependsOn(":narada:assembleDebug", ":mahati:assembleDebug")
}

tasks.register<Exec>("installNaradaApk") {
    group = "verification"
    description = "Install Narada APK on connected device"

    dependsOn("buildIntegrationTestApks")

    // Device priority: NARADA_DEVICE > ANDROID_SERIAL > auto-detect
    val deviceSerial = System.getenv("NARADA_DEVICE") ?: System.getenv("ANDROID_SERIAL")
    val adbCommand = if (deviceSerial != null) {
        listOf("adb", "-s", deviceSerial, "install", "-r", "narada/build/outputs/apk/debug/narada-debug.apk")
    } else {
        listOf("adb", "install", "-r", "narada/build/outputs/apk/debug/narada-debug.apk")
    }
    commandLine(adbCommand)

    doFirst {
        if (deviceSerial != null) {
            val source = if (System.getenv("NARADA_DEVICE") != null) "NARADA_DEVICE" else "ANDROID_SERIAL"
            println("Installing Narada on device: $deviceSerial (from $source)")
        } else {
            println("Installing Narada on default device (set NARADA_DEVICE or ANDROID_SERIAL to specify)")
        }
    }
}

tasks.register<Exec>("installMahatiApk") {
    group = "verification"
    description = "Install Mahati APK on connected device"

    dependsOn("buildIntegrationTestApks")

    // Device priority: MAHATI_DEVICE > ANDROID_SERIAL > auto-detect
    val deviceSerial = System.getenv("MAHATI_DEVICE") ?: System.getenv("ANDROID_SERIAL")
    val adbCommand = if (deviceSerial != null) {
        listOf("adb", "-s", deviceSerial, "install", "-r", "mahati/build/outputs/apk/debug/mahati-debug.apk")
    } else {
        listOf("adb", "install", "-r", "mahati/build/outputs/apk/debug/mahati-debug.apk")
    }
    commandLine(adbCommand)

    doFirst {
        if (deviceSerial != null) {
            val source = if (System.getenv("MAHATI_DEVICE") != null) "MAHATI_DEVICE" else "ANDROID_SERIAL"
            println("Installing Mahati on device: $deviceSerial (from $source)")
        } else {
            println("Installing Mahati on default device (set MAHATI_DEVICE or ANDROID_SERIAL to specify)")
        }
    }
}

tasks.register("installIntegrationTestApks") {
    group = "verification"
    description = "Install both Narada and Mahati APKs on connected device"

    dependsOn("installNaradaApk", "installMahatiApk")
}

tasks.register("runAndroidIntegrationTests") {
    group = "verification"
    description = "Build, install, and run cross-app integration tests on Android"

    dependsOn("installIntegrationTestApks", ":narada:connectedAndroidTest")

    doLast {
        println("✓ Android integration tests completed successfully!")
    }
}

tasks.register("runDesktopIntegrationTests") {
    group = "verification"
    description = "Run cross-app integration tests on Desktop"

    dependsOn(":narada:desktopTest")

    doLast {
        println("✓ Desktop integration tests completed successfully!")
    }
}

tasks.register("runAllIntegrationTests") {
    group = "verification"
    description = "Run both Android and Desktop integration tests"

    dependsOn("runAndroidIntegrationTests", "runDesktopIntegrationTests")

    doLast {
        println("✓ All integration tests completed successfully!")
    }
}

