enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "test-build"

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://nexus.figure.com/repository/mirror")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
        maven { // needed for p8e-gradle-plugin
            url = uri("https://nexus.figure.com/repository/figure")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://nexus.figure.com/repository/mirror")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
        maven { // needed for p8e-gradle-plugin
            url = uri("https://nexus.figure.com/repository/figure")
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASS")
            }
        }
        gradlePluginPortal()
    }
}
