@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    id("com.vanniktech.maven.publish")
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
        minSdk = 23
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set(project.name)
        description.set("A description of what my library does.")
        inceptionYear.set("2020")
        url.set("https://github.com/lynnfield/components/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("genovich")
                name.set("Vladimir Genovihc")
                url.set("https://github.com/lynnfield/")
            }
        }
        scm {
            url.set("https://github.com/lynnfield/components/")
            connection.set("scm:git:git://github.com/lynnfield/components.git")
            developerConnection.set("scm:git:ssh://git@github.com/lynnfield/components.git")
        }
    }
}