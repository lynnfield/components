import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
}

group = "com.genovich.components"

kotlin {
    js(IR) {
        browser()
    }
    wasmJs {
        browser()
    }
    androidTarget {
        publishLibraryVariants("release", "debug")

        @Suppress("OPT_IN_USAGE") compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()
}

android {
    namespace = "com.genovich.arch"
    compileSdk = 36
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = 24
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}