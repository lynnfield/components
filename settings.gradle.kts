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
    ":on-demand-feature",
    ":one-of",
    ":parallel",
    ":show-progress",
    ":try",
    ":try-again",
    ":uistate",
    ":update-loop",
    ":while-active",
)
