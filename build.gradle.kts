import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("multiplatform") version "1.4.30-RC"
    id("org.jetbrains.dokka") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    `maven-publish`
}

group = "com.github.DetachHead"
version = "1.0.8-SNAPSHOT"

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
