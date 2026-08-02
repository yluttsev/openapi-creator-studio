plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.24.0"
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
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
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
