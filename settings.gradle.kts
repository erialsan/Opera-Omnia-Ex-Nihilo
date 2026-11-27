pluginManagement {
  repositories {
    maven {
      // RetroFuturaGradle
      name = "GTNH Maven"
      url = uri("https://nexus.gtnewhorizons.com/repository/public/")
      mavenContent {
        includeGroup("com.gtnewhorizons")
        includeGroupByRegex("com\\.gtnewhorizons\\..+")
      }
    }
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
  }
}

plugins {
  id("com.gtnewhorizons.gtnhsettingsconvention") version ("2.0.34")
  // Resolve Kotlin at the same scope as the convention, which also supplies the plugin.
  id("org.jetbrains.kotlin.jvm") version "2.4.10" apply false
}
