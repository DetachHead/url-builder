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
}