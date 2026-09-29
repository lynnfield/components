plugins {
    `component-plugin`
}


kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlin.coroutines.core)
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