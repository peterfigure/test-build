// https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
plugins {
  `kotlin-dsl`
}

repositories {
    mavenLocal()
    // The org.jetbrains.kotlin.jvm plugin requires a repository
    // where to download the Kotlin compiler dependencies from.
    mavenCentral()
    maven {
        url = uri("https://nexus.figure.com/repository/mirror")
        credentials {
            username = findProperty("nexusUser")?.toString() ?: System.getenv("NEXUS_USER")
            password = findProperty("nexusPass")?.toString() ?: System.getenv("NEXUS_PASS")
        }
    }
}