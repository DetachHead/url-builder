import kotlin.test.Test
import kotlin.test.assertEquals

class test {
    @Test
    fun test() {
        assertEquals(
            "https://foo.com:443/asdf/sdfg?foo=bar&ssd=dfg",
            URLbuilder(Scheme.https, "foo.com") {
                "asdf" / "sdfg" params mapOf("foo" to "bar", "ssd" to "dfg")
            }.toString()
        )
    }

    @Test
    fun encode_test() {
        assertEquals(
            "http://foo.com:80/asdf%20asdf?foo%20bar=1",
            URLbuilder(Scheme.http, "foo.com") { "asdf asdf" params ("foo bar" to "1") }.toString()
        )
    }
}