import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencymanager)
}

group = "com.koog.examples"
version = "0.0.1-SNAPSHOT"


repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.10.2"))
    implementation(libs.spring.boot.starter)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.koog.spring.boot.starter)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)

    developmentOnly(libs.spring.boot.devtools)
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlinx" &&
                requested.name.startsWith("kotlinx-coroutines")) {
                useVersion("1.10.2")
                because("Force all kotlinx-coroutines to 1.10.2 for Koog compatibility")
            }
            if (requested.group == "org.jetbrains.kotlinx" &&
                requested.name.startsWith("kotlinx-serialization")) {
                useVersion("1.8.1")
                because("Force all kotlinx-serialization to 1.8.1 for Koog compatibility")
            }
        }

        force(
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2",
            "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2",
            "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2",
            "org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.10.2",
            "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1",
            "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1",
            "org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1",
            "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.8.1"
        )
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("com.koog.examples.ApplicationKt")
}

tasks.register<Copy>("getDependencies") {
    dependsOn("build")
    from(configurations.runtimeClasspath)
    into("runtime/")
    doFirst {
        val runtimeDir = File("runtime")
        runtimeDir.deleteRecursively()
        runtimeDir.mkdir()
    }
    doLast {
        println("Dependencies copied to runtime/")
    }
}
