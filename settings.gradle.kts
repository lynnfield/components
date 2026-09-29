rootProject.name = "components"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    ":action",
    ":combine",
    ":one-of",
    ":parallel",
    ":try",
    ":uistate",
    ":update-loop",
    ":while-active",
)
