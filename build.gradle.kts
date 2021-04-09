import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("multiplatform") version "1.4.30-RC"
    id("org.jetbrains.dokka") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    `maven-publish`
}

val gitRepo = "DetachHead/url-builder"
val gitURL = "https://github.com/$gitRepo"
val publicationName = "url-builder"

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
    configure(listOf(targets["metadata"], jvm(), js())) {
        mavenPublication {
            val targetPublication = this@mavenPublication
            tasks.withType<AbstractPublishToMaven>()
                .matching { it.publication == targetPublication }
                .all { onlyIf { findProperty("isMainHost") == "true" } }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
        publishing.publications.map {
            it.name
        }.find {
            it != "kotlinMultiplatform"
        }
    }
}

afterEvaluate {
    configure<PublishingExtension> {
        publications.all {
            val mavenPublication = this as? MavenPublication
            mavenPublication?.artifactId =
                "${project.name}${"-$name".takeUnless { "kotlinMultiplatform" in name }.orEmpty()}"
        }
    }
}

val mavenLocalUrl = "file://${System.getenv("HOME")}/.m2/repository"

configure<PublishingExtension> {
    publications {
        withType<MavenPublication> {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
    repositories {
        // publish locally, then a github action pushes it to a different git repo where i'm using github pages as a maven repo
        // publishToMavenLocal doesn't seem to work, it doesn't create the js and jvm publications for some reason.
        // that's why we're running publish instead, and just setting the maven repo to a local file
        //
        // also, can't use RepositoryHandler.mavenLocal because for some reason it causes the publish.doLast below to fail
        // with a `Cannot call Task.dependsOn(Object...) on task after task has started execution.` error
        maven(mavenLocalUrl)
    }
}

// https://github.com/jitpack/jitpack.io/issues/4091#issuecomment-562824426
if (System.getenv("JITPACK") == "true")
    tasks["publish"].doLast {
        val commit = System.getenv("GIT_COMMIT")
        val artifacts = publishing.publications.filterIsInstance<MavenPublication>().map { it.artifactId }

        val dir: File = File(publishing.repositories.maven(mavenLocalUrl).url)
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
                val newGroup = "com.github.DetachHead.${project.name}"
                val text = it.readText()
                println("Replacing ${project.version} with $commit and ${project.group} with $newGroup in $it")
                it.writeText(
                    text
                        .replace(project.version.toString(), commit)
                        .replace(project.group.toString(), newGroup)
                )
            }
    }
