import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal actual fun encodeURLsegment(segment: String): String =
    URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20") //https://stackoverflow.com/a/4737967