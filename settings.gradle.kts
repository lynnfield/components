rootProject.name = "components"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":action",
    ":combine",
    ":one-of",
    ":parallel",
    ":uistate",
    ":update-loop",
    ":while-active",
)
