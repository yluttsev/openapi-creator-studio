plugins {
    `java-library`
}

dependencies {
    api(project(":studio-core"))

    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
}
