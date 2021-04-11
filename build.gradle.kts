plugins {
    java
    kotlin("multiplatform") version "1.4.30-RC"
    id("org.jetbrains.dokka") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
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
                implementation(kotlin("test-js"))
            }
        }
    }
}

if (System.getenv("JITPACK") == "true")
    tasks["publishToMavenLocal"].doLast {
        val version = System.getenv("VERSION")
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
                println("Replacing ${project.version} with $version in $it")
                it.writeText(text.replace(project.version.toString(), version))
            }
    }
