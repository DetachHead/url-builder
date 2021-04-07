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
version = "1.0.7-SNAPSHOT"

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
