plugins {
    // This is necessary to avoid the plugins from being loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

repositories {
    maven {
        url = uri("https://repo.maven.apache.org/maven2")
    }
    maven {
        url = uri("https://services.gradle.org")
    }
}

gradle.allprojects {
    // Proxy configuration (use this approach in Kotlin DSL)
    if (hasProperty("http.proxyHost") && hasProperty("http.proxyPort")) {
        val proxyHost: String by project
        val proxyPort: String by project
        System.setProperty("http.proxyHost", proxyHost)
        System.setProperty("http.proxyPort", proxyPort)
    }
}
