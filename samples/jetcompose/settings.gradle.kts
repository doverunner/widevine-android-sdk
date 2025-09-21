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
//        maven {
//            name = "GitHubPackages"
//            url = uri("https://maven.pkg.github.com/doverunner/widevine-android-sdk")
//            credentials {
//                username = "GitHub ID"
//                password = "GitHub Access Token"
//            }
//
//        }
    }
}

rootProject.name = "JetcomposeSample"
include(":app")