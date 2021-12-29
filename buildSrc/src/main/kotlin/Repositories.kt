import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

object Locations {
    const val FigureNexusMirror = "https://nexus.figure.com/repository/mirror"
    const val FigureNexusFigure = "https://nexus.figure.com/repository/figure"
    const val FigureNexusMavenReleases = "https://nexus.figure.com/repository/maven-releases"
}

fun Project.figureNexusUsername(): String? {
    return findProperty("nexusUser")?.toString() ?: System.getenv("NEXUS_USER")
}

fun Project.figureNexusPassword(): String? {
    return findProperty("nexusPass")?.toString() ?: System.getenv("NEXUS_PASS")
}

fun RepositoryHandler.mavenRepository(project: Project, repoUrl: String): MavenArtifactRepository = maven {
    url = project.uri(repoUrl)
    credentials {
        username = project.figureNexusUsername()
        password = project.figureNexusPassword()
    }
}

fun RepositoryHandler.figureNexusMirrorRepository(project: Project): MavenArtifactRepository = mavenRepository(project, Locations.FigureNexusMirror)

fun RepositoryHandler.figureNexusFigureRepository(project: Project): MavenArtifactRepository = mavenRepository(project, Locations.FigureNexusFigure)

fun RepositoryHandler.figureNexusMavenReleasesRepository(project: Project): MavenArtifactRepository = mavenRepository(project, Locations.FigureNexusMavenReleases)

fun RepositoryHandler.arrow(project: Project): MavenArtifactRepository = maven {
    url = project.uri("https://dl.bintray.com/arrow-kt/arrow-kt/")
}