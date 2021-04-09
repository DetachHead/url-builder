import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("multiplatform") version "1.4.30-RC"
    id("org.jetbrains.dokka") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    `maven-publish`
}

group = "io.github.detachhead"
version = "1.0.8-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.useIR = true
}

kotlin {
    explicitApi()
    js(IR) { nodejs() }
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

if (System.getenv("JITPACK") == "true")
    tasks["publishToMavenLocal"].doLast {
        val commit = System.getenv("GIT_COMMIT")
        val artifacts = publishing.publications.filterIsInstance<MavenPublication>().map { it.artifactId }

        val dir: File = File(publishing.repositories.mavenLocal().url)
            .resolve(project.group.toString().replace('.', '/'))

        dir.listFiles { it -> it.name in artifacts }
            .flatMap {
                (
                    it.listFiles { it -> it.isDirectory }?.toList()
                        ?: emptyList<File>()
                    ) + it.resolve("maven-metadata-local.xml")
            }
            .flatMap {
                if (it.isDirectory) {
                    it.listFiles { it ->
                        it.extension == "module" ||
                            "maven-metadata" in it.name ||
                            it.extension == "pom"
                    }?.toList() ?: emptyList()
                } else listOf(it)
            }
            .forEach {
                val text = it.readText()
                val newName = "com.github.DetachHead.${project.name}"
                println("Replacing ${project.version} with $commit and ${project.group} with $newName in $it")
                it.writeText(
                    text
                        .replace(project.version.toString(), commit)
                        .replace(project.group.toString(), newName)
                )
            }
    }
