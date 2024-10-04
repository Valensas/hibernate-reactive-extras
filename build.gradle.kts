import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.regex.Pattern.compile

plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jmailen.kotlinter") version "4.4.1"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.3"
    id("jacoco")
    id("maven-publish")

}
description = "This library provides a reactive Hibernate repository for non-blocking CRUD operations"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.getByName<Jar>("jar") {
    archiveClassifier.set("")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation ("org.reflections:reflections:0.9.12")


    /* hibernate reactive */
    implementation("org.hibernate.reactive:hibernate-reactive-core:2.2.2.Final")
    implementation("io.vertx:vertx-pg-client:4.3.5")
    implementation("com.ongres.scram:common:2.1")
    implementation("com.ongres.scram:client:2.1")
    implementation("io.vertx:vertx-core:4.3.5")
    //implementation("io.vertx:vertx-oracle-client:4.3.5")
    implementation("io.vertx:vertx-sql-client:4.3.5")
    implementation("io.smallrye.reactive:mutiny-kotlin:1.6.0")
    implementation("io.smallrye.reactive:mutiny-reactor:1.7.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation ("com.squareup.okhttp3:mockwebserver:4.9.3")
    testImplementation("org.testcontainers:junit-jupiter")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    /* kotest */
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.1")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    testImplementation("org.postgresql:postgresql")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")

}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.97".toBigDecimal()
            }
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

signing {
    val keyId = System.getenv("SIGNING_KEYID")
    val secretKey = System.getenv("SIGNING_SECRETKEY")
    val passphrase = System.getenv("SIGNING_PASSPHRASE")

    useInMemoryPgpKeys(keyId, secretKey, passphrase)
}

centralPortal {
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")

    pom {
        name = "Hibernate Reactive"
        url = "https://valensas.com/"
        scm {
            url = "https://github.com/Valensas/hibernate-reactive-extras"
        }

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("0")
                name.set("Valensas")
                email.set("info@valensas.com")
            }
        }
    }
}

