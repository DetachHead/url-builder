external fun encodeURIComponent(uri: String): String

actual fun encodeURLsegment(segment: String) = encodeURIComponent(segment)