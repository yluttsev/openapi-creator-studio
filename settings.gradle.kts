rootProject.name = "openapi-creator-studio"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

include(
    "studio-core",
    "studio-openapi",
    "studio-app",
)
