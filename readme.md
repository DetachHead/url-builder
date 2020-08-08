# URL builder
an intuitive URL builder for kotlin
### example
```kotlin
assertEquals(
    "https://foo.com:443/asdf/sdfg?foo=bar&ssd=dfg",
    urlbuilder(Scheme.https, "foo.com") {
        "asdf" / "sdfg" / mapOf("foo" to "bar", "ssd" to "dfg")
    }.toString()
)
```

### disclaimer
this is very early in development and is missing functionality such as encoding and fragments
