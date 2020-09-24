# URL builder
an intuitive URL builder for kotlin. currently supports JVM and JS targets
### example
```kotlin
assertEquals(
    "https://foo.com:443/asdf/sdfg?foo=bar&ssd=dfg",
    urlbuilder(Scheme.https, "foo.com") {
        "asdf" / "sdfg" params mapOf("foo" to "bar", "ssd" to "dfg")
    }.toString()
)
```