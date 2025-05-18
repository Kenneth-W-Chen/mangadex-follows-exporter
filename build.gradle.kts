plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
}

group = "io.github.kenneth-w-chen.mangadex-follows-exporter"
version = "1.0"

repositories {
    mavenCentral()
}
val ktor_version: String by project
dependencies {
    implementation("io.ktor:ktor-client-core:${ktor_version}")
    implementation("io.ktor:ktor-client-cio:${ktor_version}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktor_version}")
    implementation("io.ktor:ktor-client-auth:${ktor_version}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
tasks.withType<Jar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    manifest{
        attributes["Main-Class"] = "MainKt"
    }
}
