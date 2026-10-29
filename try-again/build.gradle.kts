plugins {
    `component-plugin`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlin.coroutines.core)
                api(projects.action)
                api(projects.combine)
                api(projects.oneOf)
                api(projects.uistate)
                // updateLoopUntil is inline, so its body is compiled into TryAgain.
                api(projects.updateLoop)
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
