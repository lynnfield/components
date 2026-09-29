plugins {
    `component-plugin`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // updateLoop/updateLoopUntil are inline, so their bodies are compiled into the caller.
                api(libs.kotlin.coroutines.core)
                api(projects.whileActive)
                api(projects.oneOf)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
