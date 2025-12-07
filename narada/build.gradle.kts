plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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

                implementation(libs.moquette.broker)
                implementation(libs.androidx.navigation.compose)
                implementation(libs.androidx.datastore.preferences)
                api(libs.kotlinx.coroutines.core)
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
                implementation(libs.androidx.navigation.compose)

                // Android DataStore (correct place for these)
                implementation(libs.androidx.datastore)
                implementation(libs.androidx.datastore.preferences)

                implementation(libs.accompanist.permissions)
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
    namespace = "in.eswarm.narada"
    compileSdk = 36

    defaultConfig {
        applicationId = "in.eswarm.narada"
        minSdk = 23
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packagingOptions {
        resources {
            excludes += "META-INF/license/*"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
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
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "Narada"
            packageVersion = "1.0.0"
        }
    }
}

dependencies { // Root level dependencies block
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
