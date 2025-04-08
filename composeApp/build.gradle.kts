import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("com.google.dagger.hilt.android") version "2.51.1"
    id("kotlin-kapt")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kapt {
    correctErrorTypes = true
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {

        androidMain.dependencies {

            implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.*"))))

            // Azure cloud
            implementation("com.microsoft.azure.sdk.iot:iot-device-client:2.5.0") {
                exclude(module = "azure-storage")
                exclude(module = "slf4j-api")
            }
            implementation("org.slf4j:slf4j-android:1.7.36")
            implementation("com.microsoft.azure.android:azure-storage-android:2.0.0@aar")

            //android support
            implementation("com.android.support:multidex:1.0.3")
            implementation("androidx.core:core-ktx:1.13.1")
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
            implementation("androidx.activity:activity-compose:1.9.0")
            implementation("com.google.code.gson:gson:2.11.0")
            implementation("androidx.test.ext:junit-ktx:1.2.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
            implementation("com.android.volley:volley:1.2.1")
            implementation("androidx.preference:preference-ktx:1.2.1")
            implementation("io.coil-kt:coil-compose:2.6.0")
            implementation("net.sourceforge.jtds:jtds:1.3.1")
            implementation("androidx.compose.material3:material3:1.2.1")
            implementation("javax.xml.stream:stax-api:1.0-2")

            //barcode scanner libraries
            implementation("androidx.camera:camera-core:1.4.0")
            implementation("androidx.camera:camera-camera2:1.4.0")
            implementation("androidx.camera:camera-lifecycle:1.4.0")
            implementation("androidx.camera:camera-view:1.4.0")
            implementation("androidx.camera:camera-extensions:1.4.0")
            implementation("com.google.mlkit:barcode-scanning:17.3.0")

            // Compose
            val compose_version = "1.6.7"
            //debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
            //debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")
            implementation("androidx.compose.ui:ui:$compose_version")
            implementation("androidx.compose.material:material:$compose_version")
            implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
            implementation("androidx.compose.runtime:runtime:$compose_version")
            implementation("androidx.compose.runtime:runtime-rxjava2:$compose_version")
            //androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
            implementation("com.google.android.material:material:1.12.0")

            // Jalali datePicker
            implementation("com.github.hamooo90:jalali-datepicker-compose:1.1.1")
            implementation("ir.huri:JalaliCalendar:1.3.3")
            // Hilt
            implementation("com.google.dagger:hilt-android:2.51.1")
            // Sentry
            implementation("io.sentry:sentry-android:6.8.0")
            // refresh by swipe
            implementation("com.google.accompanist:accompanist-swiperefresh:0.30.1")
            // Serializable
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            // Navigation Component
            implementation("androidx.navigation:navigation-compose:2.8.5")

        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            api(libs.kotlinx.serialization.json)
            implementation(libs.bundles.ktor)
            implementation(libs.coil.compose.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.mp)
            implementation(libs.coil.network.ktor)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "io.domil.store"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.domil.store"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    "kapt"("com.google.dagger:hilt-compiler:2.51.1")
}



