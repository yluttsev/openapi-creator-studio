plugins {
    `java-library`
}

tasks.named<JacocoReport>("jacocoTestReport") {
    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it) {
                exclude("ru/luttsev/studio/core/model/**")
            }
        }
    )
}
