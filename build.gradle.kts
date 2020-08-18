import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    java
    kotlin("multiplatform") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
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