import kotlin.test.Test
import kotlin.test.assertEquals

class test {
    @Test
    fun test() {
        assertEquals(
            "https://foo.com:443/asdf/sdfg?foo=bar&ssd=dfg",
            urlbuilder(Scheme.https, "foo.com") {
                "asdf" / "sdfg" / mapOf("foo" to "bar", "ssd" to "dfg")
            }.toString()
        )
    }

    @Test
    fun encode_test() {
        assertEquals(
            "http://foo.com:80/asdf%20asdf?foo%20bar=1",
            urlbuilder(Scheme.http, "foo.com") { "asdf asdf" / ("foo bar" to "1") }.toString()
        )
    }
}