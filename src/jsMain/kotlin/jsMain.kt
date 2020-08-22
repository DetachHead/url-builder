private external fun encodeURIComponent(uri: String): String

internal actual fun encodeURLsegment(segment: String): String = encodeURIComponent(segment)