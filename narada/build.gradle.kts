plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}


kotlin {
    androidTarget()
    jvm("desktop")

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

                implementation("com.github.moquette-io:moquette:0.18.0") {
                    exclude(group = "org.slf4j", module = "slf4j-log4j12")
                    exclude(group = "org.slf4j", module = "slf4j-reload4j")
                }
                implementation(libs.navigation.compose)
                api(libs.kotlinx.coroutines.core)

                implementation(libs.androidx.datastore)
                implementation(libs.androidx.datastore.preferences)

                // For QR Code generation
                implementation(libs.custom.qr.generator)
                implementation(libs.kotlinx.serialization.json)

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

                implementation(libs.accompanist.permissions)

                // For QR Code scanning
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
                //implementation(compose.desktop.currentOs)
                //implementation(compose.desktop.common)
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
            if (project.hasProperty("NARADA_STORE_FILE")) {
                storeFile = file(project.property("NARADA_STORE_FILE") as String)
                storePassword = project.property("NARADA_STORE_PASSWORD") as String
                keyAlias = project.property("NARADA_KEY_ALIAS") as String
                keyPassword = project.property("NARADA_KEY_PASSWORD") as String
            }
        }
    }
    namespace = "in.eswarm.narada"
    compileSdk = 36

    defaultConfig {
        applicationId = "in.eswarm.narada"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
    }
    /*
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

     */
    packaging {
        resources {
            excludes += "META-INF/license/*"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

compose.resources {
    generateResClass = always
    packageOfResClass = "in.eswarm.narada.resources"
}

compose.desktop {
    application {
        mainClass = "in.eswarm.narada.desktop.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "Narada"
            packageVersion = "1.0.0"
        }
    }
}

dependencies { // Root level dependencies block
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
