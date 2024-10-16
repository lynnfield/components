import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

group = "com.genovich.components"
version = "1.0"

kotlin {
    js(IR) {
        browser()
    }
    androidTarget {
        publishLibraryVariants("release", "debug")

        @Suppress("OPT_IN_USAGE")
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.coroutines.core)
            }
        }
    }
}

android {
    namespace = "com.genovich.arch"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}