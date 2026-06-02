plugins {
	java
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
	// 벤더링한 synapse-shared Avro 계약에서 SpecificRecord 클래스를 생성한다.
	id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "io.synapse"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	// Confluent serializer는 Maven Central이 아니라 Confluent 저장소에서 받는다.
	maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.springframework.data:spring-data-commons")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
	implementation("org.mapstruct:mapstruct:1.6.3")
	// Kafka 이벤트 value는 EVENT_CONTRACT_STANDARD 기준으로 Avro + Schema Registry를 사용한다.
	implementation("org.apache.avro:avro:1.11.3")
	implementation("io.confluent:kafka-avro-serializer:7.5.0")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
	testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Spring Modulith
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:2.0.5")
    }
}
