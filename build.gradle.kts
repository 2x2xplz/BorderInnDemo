import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.example"
version = "0.1.2"


repositories {
    mavenCentral()
    maven { url = uri("file://E:/Kotlin/Hoplite/hoplite") }
    flatDir {
        dirs("E:/Kotlin/Hoplite/hoplite")
    }
}

dependencies {
    val kotlinxSerVersion = "1.2.2"
    val ktormVersion = "3.4.1"
    val http4kVersion = "4.14.0.0"
    val hopliteVersion = "1.4.4"

    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerVersion")

    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.ktorm:ktorm-support-postgresql:$ktormVersion")
    implementation("org.postgresql:postgresql:42.2.23")  // Postgres driver Java 8+
    implementation("com.h2database:h2:1.4.200") // H2 DB driver (for mocking)
    implementation("com.zaxxer:HikariCP:5.0.0")  // Hikari connection pool

    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-undertow:$http4kVersion")
    implementation("org.http4k:http4k-format-kotlinx-serialization:$http4kVersion")
    implementation("org.http4k:http4k-serverless-lambda:$http4kVersion")
    implementation("org.http4k:http4k-client-okhttp:$http4kVersion")

    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-yaml:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-json:$hopliteVersion")

    testImplementation(kotlin("test"))
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}


val shadowJar: ShadowJar by tasks
shadowJar.apply {

    manifest.attributes.apply {
        put("Manifest-Version", 1.0)
        put("Implementation-Version", project.version)
        put("Main-Class", "MainKt")
    }

    exclude("hoplite_*.*")
    exclude("config_*.*")

    archiveFileName.set("BorderInnDirectionsHT.jar")
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}