
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    androidTarget {

    }

    jvm("desktop") {

    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.components.resources)
                // JetBrains Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3) // JetBrains Material 3 for KMP
                implementation(compose.ui)
                implementation(compose.components.resources) // For common resources (images, fonts)
                implementation(compose.components.uiToolingPreview) // For @Preview in commonMain
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.navigation.compose)

                // Kotlinx Coroutines
                api(libs.kotlinx.coroutines.core)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                // Multiplatform Settings (example, if you need KMP settings)
                // api(libs.multiplatform.settings.no.arg)
                // api(libs.multiplatform.settings.coroutines)

                // HiveMQ MQTT Client (assuming it's pure Java/Kotlin and platform-agnostic enough for common JVM)
                // If it has platform-specific dependencies, it may need expect/actual or to be in platform-specific source sets.
                api(libs.hivemq.mqtt.client)
            }
        }

        val androidMain by getting {
            dependencies {
                // AndroidX libraries needed for the Android target
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.activity.compose) // For ComponentActivity and Android UI hosting
                implementation(libs.androidx.lifecycle.runtime.ktx.mahati) // Your existing alias
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.navigation.compose) // For AndroidX Navigation

                // Android DataStore (if used in androidMain and not via a KMP settings lib)
                implementation(libs.androidx.datastore.preferences)

                // Android specific tooling for previews if needed beyond common uiToolingPreview
                // debugImplementation(libs.androidx.compose.ui.tooling)
            }
        }

        val desktopMain by getting {
            dependencies {
                // This pulls in the necessary Compose for Desktop artifacts (runtime, UI, etc.)
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.common)  // For common desktop utilities if needed
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit")) // For running common tests on JVM
            }
        }

        val androidUnitTest by getting { // Local unit tests for androidMain
            dependencies {
                implementation(libs.junit) // For JUnit tests
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test-junit")) // For JUnit tests on Desktop
            }
        }
        // Instrumented tests for Android (androidTest) would be configured inside androidTarget if needed
    }
}

android {
    namespace = "in.eswarm.mahati" // Stays as it's an Android library
    compileSdk = 36

    defaultConfig {
        applicationId = "in.eswarm.mahati"
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // For instrumented tests
    }
    buildTypes {
        release {
            isMinifyEnabled = false // Adjust as needed
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    packagingOptions { // Keep existing packaging options
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
    buildFeatures {
        compose = true // Explicitly enable Compose for the Android app part
    }
    composeOptions { // Set the Kotlin Compose Compiler version
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    // Lint options if needed
    lint {
        abortOnError = false
    }
}

compose.resources {
    generateResClass = always
    packageOfResClass = "in.eswarm.mahati.resources"
}

compose.desktop {
    application {
        mainClass = "in.eswarm.mahati.desktop.MainKt" // Adjust if your desktop main is elsewhere
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "Mahati"
            packageVersion = "1.0.0"
        }
    }
}


dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}