plugins {
  id("com.gtnewhorizons.gtnhconvention")
  kotlin("jvm")
}

kotlin {
  // Match GTNH's Java compile target; JvmDowngrader targets Java 8 for distribution.
  compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
}

dependencies {
  compileOnly(kotlin("stdlib"))
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin {
    // The inherited ktfmt 0.39 cannot parse GraalVM's JDK 25 version string.
    clearSteps()
    toggleOffOn()
    ktfmt().kotlinlangStyle()
    trimTrailingWhitespace()
    leadingTabsToSpaces(4)
    endWithNewline()
  }
}

// Dependencies and fixtures for the iron trials are opt-in, never part of a normal build.
providers.gradleProperty("ooenProbe").orNull?.let { profile ->
  val probes =
      when (profile) {
        "materialslib",
        "jaopca" -> listOf(profile)
        "both" -> listOf("materialslib", "jaopca")
        else -> error("Unknown ooenProbe '$profile': use materialslib, jaopca or both")
      }
  probes.forEach { apply(from = "probes/$it/probe.gradle") }
}
