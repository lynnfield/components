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
                api(projects.parallel)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(projects.uistate)
            }
        }
    }
}
