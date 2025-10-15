plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.0"
    java
}

group = "ru.offer.hunt.course"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:3.3.2")

    // --- Database & migrations ---
    implementation("org.flywaydb:flyway-core:10.15.0")
    runtimeOnly("org.postgresql:postgresql:42.7.4")

    // --- JWT / JOSE ---
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.2")
    testImplementation("org.springframework.security:spring-security-test:6.3.2")
    testImplementation("org.testcontainers:junit-jupiter:1.20.3")
    testImplementation("org.testcontainers:postgresql:1.20.3")
}

tasks.test {
    useJUnitPlatform()
}