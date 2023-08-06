plugins {
    kotlin("multiplatform") version "1.9.0"
    id("org.jetbrains.dokka") version "1.8.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
    `maven-publish`
}

group = "io.github.detachhead"
version = "1.0.7"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    explicitApi()
    js { nodejs() }
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

val publishToGithubPages: Task by tasks.creating {
    group = "publishing"
    doFirst {
        val publishLocation = File(publishing.repositories.mavenLocal().url)
            .resolve("${project.group.toString().replace('.', '/')}/${project.name}")
        if (!version.toString()
            .endsWith("-SNAPSHOT") &&
            publishLocation.list()?.contains(version) == true
        )
            error("$version has already been published")
    }
    finalizedBy(tasks.publishToMavenLocal)
}
