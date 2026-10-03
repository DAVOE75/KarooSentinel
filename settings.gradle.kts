import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            name = "KarooExt"
            url = uri("https://maven.pkg.github.com/hammerheadnav/karoo-ext")

            // Leer credenciales de forma segura
            val localProperties = Properties()
            val localPropertiesFile = rootDir.resolve("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(localPropertiesFile.inputStream())
            }

            credentials {
                username = localProperties.getProperty("gpr.user") ?: System.getenv("GITHUB_USER")
                password = localProperties.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "ALTGRAPH"
include(":app")