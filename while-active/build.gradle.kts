plugins {
    `component-plugin`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // whileActive is inline, so its body is compiled into the caller.
                api(libs.kotlin.coroutines.core)
            }
        }
    }
}
