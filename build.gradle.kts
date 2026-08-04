plugins {
    base
    alias(libs.plugins.sonarqube)
}

allprojects {
    group = "ru.luttsev"
    version = "0.1.0-SNAPSHOT"
}

sonar {
    properties {
        property("sonar.projectKey", "yluttsev_openapi-creator-studio")
        property("sonar.organization", "yluttsev")
    }
}

subprojects {
    pluginManager.withPlugin("java") {
        apply(plugin = "jacoco")

        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(25)
            }
        }

        extensions.configure<JacocoPluginExtension> {
            toolVersion = "0.8.14"
        }

        dependencies {
            add("testImplementation", platform("org.junit:junit-bom:6.0.0"))
            add("testImplementation", "org.junit.jupiter:junit-jupiter")
            add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }

        tasks.withType<JacocoReport>().configureEach {
            // classDirectories reads compileJava's output; normally that
            // ordering is only implied via jacocoTestReport -> test ->
            // compileJava. CI runs the Sonar scan with `-x test` (test
            // already ran in an earlier job), which drops that implicit
            // link and trips Gradle's task validation - depend on it
            // explicitly instead.
            dependsOn(tasks.named("compileJava"))
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }

        tasks.named("check") {
            dependsOn(tasks.named("jacocoTestReport"))
        }

        rootProject.tasks.named("sonar") {
            dependsOn(tasks.named("check"))
        }
    }
}
