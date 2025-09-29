plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}
val springAiVersion by extra("1.0.2")

group = "com"
version = "0.0.1-SNAPSHOT"
description = "backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.ai:spring-ai-starter-model-vertex-ai-gemini")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-actuator") // 추가됨
    implementation("org.springframework.boot:spring-boot-starter-data-redis") // 추가됨
    implementation("org.springframework.session:spring-session-data-redis") // 추가됨


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    implementation("org.springframework.boot:spring-boot-starter-websocket")

    implementation ("org.springframework.kafka:spring-kafka")


    // 1. Spring AI의 모든 모듈 버전을 일관되게 관리하는 BOM (필수)
    implementation(enforcedPlatform("org.springframework.ai:spring-ai-bom:1.0.2-M1"))
    // 2. Vertex AI Gemini 모델 스타터
  //  implementation("org.springframework.ai:spring-ai-vertex-ai-gemini-spring-boot-starter")

}
dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
