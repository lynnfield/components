plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    //noinspection AndroidGradlePluginVersion
    implementation("com.android.tools.build:gradle:8.6.0")
}
