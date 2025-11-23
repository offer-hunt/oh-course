plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.0"
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

group = "ru.offer.hunt.course"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:3.3.2")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.projectlombok:lombok:1.18.38")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // --- Database & migrations ---
    implementation("org.flywaydb:flyway-database-postgresql:11.10.0")
    runtimeOnly("org.postgresql:postgresql:42.7.4")

    // --- JWT / JOSE ---
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.2")
    testImplementation("org.springframework.security:spring-security-test:6.3.2")
    testImplementation("org.testcontainers:junit-jupiter:1.20.3")
    testImplementation("org.testcontainers:postgresql:1.20.3")
    testImplementation("org.postgresql:postgresql:42.7.4")
    testImplementation("com.h2database:h2")

    testImplementation ("org.wiremock:wiremock-jetty12:3.9.1")

    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
}

tasks.test {
    useJUnitPlatform()
}