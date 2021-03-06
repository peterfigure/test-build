import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.ZoneId

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.tasktree)
    alias(libs.plugins.testlogger)
    alias(libs.plugins.semver)
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.dependencyUpdates)
    alias(libs.plugins.dependencyCheck)
    alias(libs.plugins.githubRelease)
    alias(libs.plugins.changelog)
//    id("io.github.nefilim.gradle.catalog-plugin") version "0.0.3"
    `maven-publish`
    signing
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getenv("OSS_USER"))
            password.set(System.getenv("OSS_TOKEN"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

semver {
    tagPrefix("v")
    initialVersion("0.1.0")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
    val semVerModifier = findProperty("semver.modifier")?.toString()?.let { buildVersionModifier(it) } ?: { nextPatch() }
    versionModifier(semVerModifier)
}

version = semver.version
group = "io.github.nefilim.testbuild"

allprojects {
    apply(plugin = rootProject.libs.plugins.dependencyCheck.get().pluginId)
    apply(plugin = rootProject.libs.plugins.semver.get().pluginId)

    val jvmVersion = 11
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(jvmVersion))
            vendor.set(JvmVendorSpec.AZUL)
            implementation.set(JvmImplementation.VENDOR_SPECIFIC)
        }
    }
    // ^^^ this also sets the Kotlin toolchain, no longer needed to set the jvmTarget in the kotlinOptions
// add something

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
            languageVersion = "1.6"
            apiVersion = "1.6"
        }
    }

    dependencyCheck {
        failOnError = true

        suppressionFile = ".dependency-check-suppression.xml"
        analyzers.experimentalEnabled = false
        analyzers.assemblyEnabled = false
        analyzers.msbuildEnabled = false
        analyzers.nuspecEnabled = false
        analyzers.nugetconfEnabled = false
        analyzers.pyPackageEnabled = false
        analyzers.pyDistributionEnabled = false
        analyzers.rubygemsEnabled = false
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    listOf(
        libs.kotlinLogging
    ).map {
        implementation(it)
    }

    listOf(
        libs.kotest.runner,
        libs.kotest.assertions.core,
        libs.kotest.assertions.arrow,
    ).map {
        testImplementation(it)
    }
}

subprojects {
    // https://kotlinlang.org/docs/reference/using-gradle.html#using-gradle-kotlin-dsl
    apply {
        plugin(rootProject.libs.plugins.kotlin.jvm.get().pluginId)
        plugin(rootProject.libs.plugins.testlogger.get().pluginId)
        plugin("signing")
        plugin("maven-publish")
    }

    // required for Sonatype POM validation
    java {
        withSourcesJar()
        withJavadocJar()
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    configure<com.adarshr.gradle.testlogger.TestLoggerExtension> {
        theme = com.adarshr.gradle.testlogger.theme.ThemeType.STANDARD
        showCauses = true
        slowThreshold = 1000
        showSummary = true
        showStandardStreams = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    signing {
        val skipSigning = findProperty("skipSigning")?.let { (it as String).toBoolean() } ?: false
        if (!skipSigning) {
            val signingKeyId: String? by project
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications)
        } else {
            logger.warn("skipping signing")
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                pom {
                    name.set("kjwt-${project.name}")
                    description.set("Functional Kotlin & Arrow based library for generating and verifying JWTs and JWSs")
                    url.set("https://github.com/nefilim/kjwt")
                    licenses {
                        license {
                            name.set("GPL-3.0-only")
                            url.set("https://opensource.org/licenses/GPL-3.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("nefilim")
                            name.set("nefilim")
                            email.set("nefilim@hotmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/nefilim/kjwt.git")
                        url.set("https://github.com/nefilim/kjwt")
                    }
                }
                artifactId = "kjwt-${project.name}"
                groupId = project.group.toString()
                version = project.version.toString()
                from(components["java"])
            }
        }
    }
}

//changelog {
//    version.set(semver.calculatedTagName)
//    path.set("${project.projectDir}/CHANGELOG.md")
//    header.set(provider { "[${version.get()}] - ${date()}" })
//    itemPrefix.set("-")
//    keepUnreleasedSection.set(true)
//    unreleasedTerm.set("[Unreleased]")
//    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
//    headerParserRegex.set("""^v((0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?)${'$'}""".toRegex())
//}

val githubTokenValue = findProperty("githubToken")?.toString() ?: System.getenv("GITHUB_TOKEN")

changelog {
    githubUser = "nefilim"//
    githubToken = githubTokenValue
    githubRepository = "test-build"

    title = "Change Log"
    showUnreleased = true
    unreleasedVersionTitle = "Unreleased"
    futureVersionTag = project.version.toString()
    sections = emptyList() // no custom sections by default, but default sections are prepended
    defaultIssueSectionTitle = "Closed issues:"
    defaultPrSectionTitle = "Merged pull requests:"
    includeLabels = emptyList()
    excludeLabels = listOf("duplicate", "invalid", "question", "wontfix")
    sinceTag = null
    skipTags = emptyList()
    skipTagsRegex = emptyList()
    releaseUrlTemplate = null // defaults to "https://github.com/$user/$repo/tree/%s"
    diffUrlTemplate = null // defaults to "https://github.com/$user/$repo/compare/%s...%s"
    releaseUrlTagTransform = { it }
    diffUrlTagTransform = { it }
//    customTagByIssueNumber = [:]
    useMilestoneAsTag = true
    timezone = ZoneId.of("America/Denver")

    outputFile = file("${projectDir}/CHANGELOG.md")
    println("FUTURE: ${this.futureVersionTag}")
}

githubRelease {
    token(githubTokenValue) // This is your personal access token with Repo permissions
    // You get this from your user settings > developer settings > Personal Access Tokens
    owner("nefilim")
    repo("test-build")
    tagName.set(semver.versionTagName)
    targetCommitish("main")
    body(changelog())
    draft(false)
    prerelease(false)

    overwrite(false) 
    dryRun(false) // by default false; you can use this to see what actions would be taken without making a release
    apiEndpoint("https://api.github.com") // should only change for github enterprise users
    client // This is the okhttp client used for http requests
}

tasks.register("printstuff") {
    doLast {
        println(githubRelease.tagName.get())
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

// https://github.com/ben-manes/gradle-versions-plugin/discussions/482
tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    // reject all non stable versions
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
