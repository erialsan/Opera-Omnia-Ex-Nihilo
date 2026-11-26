
plugins {
    id("com.gtnewhorizons.gtnhconvention")
    kotlin("jvm") version "2.4.21"
}

kotlin {
    // Match GTNH's Java compile target; JvmDowngrader targets Java 8 for distribution.
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
}

dependencies {
    compileOnly(kotlin("stdlib"))
}

// Dependencies and fixtures for the iron trials are opt-in, never part of a normal build.
providers.gradleProperty("ooenProbe").orNull?.let { profile ->
    val probes = when (profile) {
        "materialslib", "jaopca" -> listOf(profile)
        "both" -> listOf("materialslib", "jaopca")
        else -> error("Unknown ooenProbe '$profile': use materialslib, jaopca or both")
    }
    probes.forEach { apply(from = "probes/$it/probe.gradle") }
}
