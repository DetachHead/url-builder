plugins {
    kotlin("multiplatform") version "1.5.0-M2"
    id("org.jetbrains.dokka") version "1.4.30"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    `maven-publish`
}

group = "io.github.detachhead"
version = "1.0.7"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    js { nodejs() }
    jvm {}
    sourceSets {
        @Suppress("unused_variable")
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        @Suppress("unused_variable")
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        @Suppress("unused_variable")
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-js"))
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
