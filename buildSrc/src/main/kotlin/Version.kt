import org.gradle.api.Project
import org.gradle.kotlin.dsl.ScriptHandlerScope

fun Project.figureArtifactVersion(project: Project): String = project.findProperty("artifactVersion")?.toString() ?: "1.0-snapshot"
fun Project.containerTags(project: Project): Set<String> = project.findProperty("containerTags")?.toString()?.split(",")?.toSet() ?: setOf()