import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

plugins {
    base
}

allprojects {
    group = "ru.luttsev"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    pluginManager.withPlugin("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(25)
            }
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
    }
}
