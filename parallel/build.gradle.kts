plugins {
    `component-plugin`
}

version = "1.0"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.coroutines.core)
            }
        }
    }
}
