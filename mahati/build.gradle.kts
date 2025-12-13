plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinxSerialization)
}

sqldelight {
    databases {
        create("MahatiDb") {
            packageName.set("in.eswarm.mahati.db")
        }
    }
}

kotlin {
    androidTarget()
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                // SQLDelight
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines.extensions)

                implementation(compose.components.resources)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.lifecycle.runtime.compose)

                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.navigation.compose)

                implementation(libs.material.icons.extended)

                api(libs.kotlinx.coroutines.core)
                api(libs.hivemq.mqtt.client)

                implementation(libs.kotlinx.serialization.json)
            }
        }

        val androidMain by getting {
            dependencies {
                // SQLDelight Android Driver
                implementation(libs.sqldelight.android.driver)

                // Dependencies from your existing file
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.runtime.ktx)

                // Android DataStore (correct place for these)
                implementation(libs.androidx.datastore)
                implementation(libs.androidx.datastore.preferences)

                // QR Code Scanning
                implementation(libs.google.mlkit.barcode.scanning)
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.view)
                implementation(libs.androidx.camera.lifecycle)
            }
        }

        val desktopMain by getting {
            dependencies {
                // SQLDelight SQLite Driver (for JVM/Desktop)
                implementation(libs.sqldelight.sqlite.driver)

                // Dependencies from your existing file
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.common)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

android {
    namespace = "in.eswarm.mahati"
    compileSdk = 36

    defaultConfig {
        applicationId = "in.eswarm.mahati"
        minSdk = 26
        targetSdk = 36 // Added targetSdk
        versionCode = 1 // Added versionCode
        versionName = "1.0" // Added versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
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
    packagingOptions {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
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
        mainClass = "in.eswarm.mahati.desktop.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "Mahati"
            packageVersion = "1.0.0"
        }
    }
}

dependencies { // Root level dependencies block
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
