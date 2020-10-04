# URL builder
an intuitive URL builder for kotlin. currently supports JVM and JS targets

## setup

```kotlin
repositories {
    maven("https://dl.bintray.com/detachhead/detach/")
}
```

### multiplatform projects

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.detachhead:url-builder:1.0.4")
            }
        }
    }
}
```

## example
```kotlin
assertEquals(
    "https://foo.com:443/asdf/sdfg?foo=bar&ssd=dfg",
    urlbuilder(Scheme.https, "foo.com") {
        "asdf" / "sdfg" params mapOf("foo" to "bar", "ssd" to "dfg")
    }.toString()
)
```