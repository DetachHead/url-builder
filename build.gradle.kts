import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("multiplatform") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.10"
    `maven-publish`
}

val gitRepo = "DetachHead/url-builder"
val gitURL = "https://github.com/$gitRepo"
val publicationName = "url-builder"

group = "io.github.detachhead"
version = "1.0.5"

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

configure<PublishingExtension> {
    publications {
        withType<MavenPublication> {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
    repositories {
        maven("https://api.bintray.com/maven/detachhead/detach/${project.name}/;publish=1") {
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_KEY")
            }
        }
    }
}