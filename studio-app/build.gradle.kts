plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

description = "OpenAPI Creator Studio web application"

dependencies {
    implementation(project(":studio-core"))
    implementation(project(":studio-openapi"))

    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.jar {
    enabled = false
}
