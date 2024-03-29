
import detachhead.urlbuilder.Scheme
import detachhead.urlbuilder.URLbuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class Tests {
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

    @Test
    fun fragment_test() {
        assertEquals(
            "http://foo.com:80#baz",
            URLbuilder(Scheme.http, "foo.com") { frag("baz") }.toString()
        )
    }

    @Test
    fun two_params_and_a_frag() {
        assertEquals(
            "http://foo.com:80?foo=bar&ssd=dfg#baz",
            URLbuilder(Scheme.http, "foo.com") {
                params("foo" to "bar", "ssd" to "dfg") frag "baz"
            }.toString()
        )
    }

    @Test
    fun one_path_param_no_port() {
        assertEquals(
            "http://foo.com:80/bar",
            URLbuilder(Scheme.http, "foo.com", "bar").toString()
        )
    }

    @Test
    fun one_path_param_with_port() {
        assertEquals(
            "http://foo.com:81/bar",
            URLbuilder(Scheme.http, "foo.com", 81, "bar").toString()
        )
    }

    @Test
    fun copying_and_mutability() {
        val first = URLbuilder(Scheme.http, "foo.com") { "a" / "b" }.build()
        val secondBuilder = URLbuilder(first)
        secondBuilder.path.removeAt(0)
        val second = secondBuilder.build()
        second.toString()
        // make sure the first one didn't change
        assertEquals("http://foo.com:80/a/b", first.toString())
        assertEquals("http://foo.com:80/b", second.toString())
    }
}
