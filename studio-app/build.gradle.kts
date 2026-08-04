plugins {
    java
    `java-test-fixtures`
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.openapi.generator)
}

description = "OpenAPI Creator Studio web application"

val restApiSpec = layout.projectDirectory.file(
    "src/main/resources/static/openapi/studio-api.yaml"
)
val generatedRestApi = layout.buildDirectory.dir("generated/openapi")

dependencies {
    implementation(project(":studio-core"))
    implementation(project(":studio-openapi"))

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.mapstruct)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor(libs.mapstruct.processor)
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation(testFixtures(project()))
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testFixturesImplementation(project(":studio-core"))
}

testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter("6.0.0")

            sources {
                java.setSrcDirs(listOf("src/integration-test/java"))
                resources.setSrcDirs(listOf("src/integration-test/resources"))
            }

            dependencies {
                implementation(project())
                implementation(testFixtures(project()))
                implementation("org.springframework.boot:spring-boot-starter-webmvc-test")
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(tasks.test)
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

tasks.jar {
    enabled = false
}

openApiValidate {
    inputSpec.set(restApiSpec.asFile.absolutePath)
    recommend.set(true)
}

openApiGenerate {
    generatorName.set("spring")
    library.set("spring-boot")
    inputSpec.set(restApiSpec.asFile.absolutePath)
    outputDir.set(generatedRestApi.get().asFile.absolutePath)
    templateDir.set(
        layout.projectDirectory.dir("src/main/openapi/templates").asFile.absolutePath
    )
    apiPackage.set("ru.luttsev.studio.generated.api")
    modelPackage.set("ru.luttsev.studio.generated.model")
    invokerPackage.set("ru.luttsev.studio.generated.invoker")
    configOptions.set(
        mapOf(
            "annotationLibrary" to "none",
            "documentationProvider" to "none",
            "hideGenerationTimestamp" to "true",
            "interfaceOnly" to "true",
            "openApiNullable" to "false",
            "performBeanValidation" to "false",
            "requestMappingMode" to "api_interface",
            "skipDefaultInterface" to "true",
            "useBeanValidation" to "true",
            "useJackson3" to "true",
            "useOneOfInterfaces" to "true",
            "useSpringBoot4" to "true",
            "useSwaggerUI" to "false",
            "useTags" to "true",
        )
    )
}

sourceSets.named("main") {
    java.srcDir(generatedRestApi.map { it.dir("src/main/java") })
}

tasks.named("openApiGenerate") {
    dependsOn(tasks.named("openApiValidate"))
    doFirst {
        delete(generatedRestApi.get().asFile)
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.named("openApiGenerate"))
}

tasks.named("check") {
    dependsOn(tasks.named("openApiValidate"))
}

// The plain `jar` task is disabled above (this app ships as a `bootJar`), so
// any project-dependency resolution (`implementation(project())`,
// `java-test-fixtures`' rewiring of `test`) that expects a jar artifact ends
// up with a missing file on the classpath instead of the compiled classes.
// Point both test sourceSets at the real `main` output directly.
sourceSets.named("test") {
    compileClasspath += sourceSets.named("main").get().output
    runtimeClasspath += sourceSets.named("main").get().output
}

sourceSets.named("integrationTest") {
    compileClasspath += sourceSets.named("main").get().output
    runtimeClasspath += sourceSets.named("main").get().output
}
