
plugins {
    id("com.gtnewhorizons.gtnhconvention")
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
