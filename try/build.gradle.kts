plugins {
    `component-plugin`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.coroutines.core)
                api(projects.action)
                api(projects.oneOf)
                implementation(projects.parallel)
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
