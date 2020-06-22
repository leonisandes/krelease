plugins {
    application
    id("jacoco")
    kotlin("jvm") version "1.3.61"
    id("org.jmailen.kotlinter") version "2.1.2"
    id("io.gitlab.arturbosch.detekt") version "1.1.1"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("com.adarshr.test-logger") version "2.0.0"
}

group = "io.io.github.leonisandes.krelease"
version = "1.0"

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Command line
    implementation("com.github.ajalt:clikt:2.7.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")

    // HTTP Client
    implementation("com.github.kittinunf.fuel:fuel:2.2.1")

    // Serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.3")

    // Mock Framework
    testImplementation("io.mockk:mockk:1.9.3")
    implementation("com.github.tomakehurst:wiremock-jre8:2.25.1")

    // Assert Framework
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("org.assertj:assertj-core:3.15.0")

    //Test Engine
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.1")
    testImplementation("org.testcontainers:junit-jupiter:1.12.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.1")
}

tasks {
    assemble { dependsOn(shadowJar) }
    assemble { dependsOn(formatKotlin) }

    application {
        mainClassName = "io.github.leonisandes.krelease.ApplicationKt"
    }

    detekt {
        failFast = true
        buildUponDefaultConfig = true
        input = files("src/main/kotlin", "src/test/kotlin")
        config = files("$projectDir/detekt/config.yml")

        reports {
            xml.enabled = true

            html.enabled = false
            txt.enabled = false
        }
    }

    kotlinter {
        ignoreFailures = true
        indentSize = 4
        continuationIndentSize = 4
        reporters = arrayOf("checkstyle", "plain")
        experimentalRules = false
        disabledRules = emptyArray()
        fileBatchSize = 30
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }

    testlogger {
        showStackTraces = false
        showCauses = false
        showSimpleNames = true
        showExceptions = true
        showPassed = false
        showStandardStreams = false
    }

    withType(Test::class.java) {
        useJUnitPlatform()
    }

    test {
        systemProperty("user.timezone", "UTC")

        System.getProperty("test.type")?.let {
            if (it == "unit") exclude("**/*integration*")
            if (it == "integration") exclude("**/*unit*")
        }
    }
}
