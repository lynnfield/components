@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
}

kotlin {
    // Consumers on an older Kotlin compiler must be able to read our metadata,
    // and must not be forced onto a newer kotlin-stdlib.
    // This is the stdlib version our published modules declare; only the JVM compiles against it (see below).
    coreLibrariesVersion = "2.1.0"
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_1
        apiVersion = KotlinVersion.KOTLIN_2_1
    }

    js(IR) {
        browser()
    }
    wasmJs {
        browser()
    }

    // Android apps consume the jvm variant, like kotlinx-coroutines-core does,
    // so there is no minSdk and no AGP in this build.
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
}

// Klib-based targets (common metadata, JS, Wasm, Native) can only read a stdlib with the
// compiler's own ABI version, so they compile against the compiler's stdlib instead of
// coreLibrariesVersion. Their consumers need this compiler version anyway to read our klibs.
val kotlinCompilerVersion = getKotlinPluginVersion()
configurations
    .matching { !it.name.startsWith("jvm") }
    .configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin" &&
                (requested.name.startsWith("kotlin-stdlib") ||
                    requested.name.startsWith("kotlin-test") ||
                    requested.name == "kotlin-dom-api-compat")
            ) {
                useVersion(kotlinCompilerVersion)
            }
        }
    }

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set(project.name)
        description.set("Suspend-function building blocks (Action, OneOf, Try, parallel, UiState) for composing app logic.")
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
