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

    // Use JDK 17 toolchain for desktop builds (includes jpackage for MSI creation)
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":shared"))
                // SQLDelight
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines.extensions)
                implementation(libs.components.resources)
                implementation(libs.runtime)
                implementation(libs.foundation)
                implementation(libs.material3)
                implementation(libs.ui)
                implementation(libs.ui.tooling.preview)
                implementation(libs.lifecycle.runtime.compose)
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.navigation.compose)
                implementation(libs.material.icons.extended)

                api(libs.kotlinx.coroutines.core)
                api(libs.hivemq.mqtt.client)
                api("io.netty:netty-codec-http:4.1.100.Final") // Add Netty HTTP codec for WebSocket support

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.slf4j.api)
                runtimeOnly("org.apache.logging.log4j:log4j-core:2.25.3")
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

                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.common)
                implementation(libs.kotlinx.coroutines.swing)

                implementation("org.xerial:sqlite-jdbc:3.51.1.0") {
                    exclude(group = "org.slf4j", module = "slf4j-log4j12")
                }
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
    signingConfigs {
        create("release") {
            if (project.hasProperty("MAHATI_STORE_FILE")) {
                storeFile = file(project.property("MAHATI_STORE_FILE") as String)
                storePassword = project.property("MAHATI_STORE_PASSWORD") as String
                keyAlias = project.property("MAHATI_KEY_ALIAS") as String
                keyPassword = project.property("MAHATI_KEY_PASSWORD") as String
            }
        }
    }
    namespace = "in.eswarm.mahati"
    compileSdk = 36

    defaultConfig {
        applicationId = "in.eswarm.mahati"
        minSdk = 26
        targetSdk = 36 // Added targetSdk
        versionCode = 2 // Added versionCode
        versionName = "1.1" // Added versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
    buildFeatures {
        compose = true
    }
    /*
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

     */
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

        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard-rules.pro"))
        }

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "Mahati"
            packageVersion = "1.0.0"

            // For MSI packaging, you need a full JDK 17+ with jpackage
            modules("java.sql")
        }
    }
}

dependencies {
    api(project(":shared"))
    // Root level dependencies block
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
