plugins {
    `component-plugin`
}

version = "1.0.0-SNAPSHOT"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.coroutines.core)
            }
        }
    }
}
