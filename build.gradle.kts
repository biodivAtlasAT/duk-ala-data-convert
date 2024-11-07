plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("com.github.johnrengelman.shadow") version "7.1.0"
}


group = "duk.at"
version = "1.0-SNAPSHOT"



repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.logging.log4j:log4j-api:2.24.0")
    implementation("org.apache.logging.log4j:log4j-core:2.24.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.24.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("duk.at.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "duk.at.MainKt"
    }
}

tasks.shadowJar {
    archiveBaseName.set("duk-ala-data-convert")
    manifest {
        attributes["Main-Class"] = "duk.at.MainKt"
    }
}

tasks.jar {
    manifest {
        attributes["Multi-Release"] = "true"
    }
}