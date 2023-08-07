plugins {
    kotlin("multiplatform") version "1.5.0-RC"
    id("org.jetbrains.dokka") version "1.4.30"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    `maven-publish`
}

group = "io.github.detachhead"
version = "2.0.0"

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
        val commonTest by getting {
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
        if (System.getenv("GITHUB_BASE_REF") == "master") {
            if (publishLocation.list()?.contains(version) == true)
                version = "$version-SNAPSHOT"
        } else {
            version = "$version-${System.getenv("GITHUB_SHA")}"
        }
    }
    finalizedBy(tasks.publishToMavenLocal)
}
