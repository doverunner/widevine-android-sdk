// Root build.gradle.kts
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}