public enum class Scheme { http, https }

/**
 * an authentication that goes before the host in a [URL]
 */
public data class authentication(val username: String, val password: String) {
    override fun toString(): String = "${encodeURLsegment(username)}:${encodeURLsegment(password)}@"
}

public data class path(val segments: List<String>) : List<String> by segments {
    public constructor(vararg segments: String) : this(segments.asList())

    override fun toString(): String =
        (if (segments.isNotEmpty()) "/" else "") + segments.joinToString("/") { encodeURLsegment(it) }
}

/**
 * [URL] query parameters
 */
public data class queryparams(val value: Map<String, String> = mapOf()) : Map<String, String> by value {
    override fun toString(): String =
        value.let {
            if (it.isNotEmpty()) it.entries.joinToString("&", "?") {
                "${encodeURLsegment(it.key)}=${encodeURLsegment(it.value)}"
            } else ""
        }
}

private typealias urlBuilderBlock = urlbuilder.() -> Unit

/**
 * builds a [URL]
 */
public class urlbuilder(
    public var scheme: Scheme,
    public var auth: authentication? = null,
    public var host: String,
    public var port: Int = defaultPort(scheme),
    block: urlBuilderBlock = {}
) {
    /**
     * constructs a [urlbuilder] without an [auth]
     */
    public constructor(scheme: Scheme, host: String, port: Int = defaultPort(scheme), block: urlBuilderBlock = {}) :
            this(scheme, null, host, port, block)

    /**
     * constructs a [urlbuilder] with a [Pair] of [String]s as the [authentication]
     */
    public constructor(
        scheme: Scheme,
        auth: Pair<String, String>,
        host: String,
        port: Int = defaultPort(scheme),
        block: urlBuilderBlock = {}
    ) : this(scheme, authentication(auth.first, auth.second), host, port, block)

    public var path: path = path()
    public var params: queryparams = queryparams()

    init {
        @Suppress("unused_expression") //https://youtrack.jetbrains.com/issue/KT-21282
        block()
    }

    /**
     * builds an immutable [URL] from the properties in the current [urlbuilder]
     */
    private fun build() = URL(scheme, auth, host, port, path, params)

    override fun toString(): String = build().toString()

    /**
     * adds the two [String]s to the [path]
     */
    public operator fun String.div(other: String): urlbuilder = this@urlbuilder.apply { this / this@div / other }

    /**
     * adds this [String] and the given [Map] of parameters to the [urlbuilder]
     */
    public operator fun String.div(other: Map<String, String>): urlbuilder =
        this@urlbuilder.apply { this / this@div / other }

    /**
     * adds this [String] and the given [Pair] of parameters to the [urlbuilder]
     */
    public operator fun String.div(other: Pair<String, String>): urlbuilder = this / mapOf(other)

    /**
     * adds the given [String] to the [path]
     */
    public operator fun div(other: String): urlbuilder = apply { path = path(path + other) }

    /**
     * takes a [Map] of [String]s and adds them to the [params] (replaces params if theyre already there)
     */
    public operator fun div(other: Map<String, String>): urlbuilder = apply { params = queryparams(params + other) }

    /**
     * takes a [Pair] of [String]s and adds them to the [params] (replaces the param if its already there)
     */
    public operator fun div(other: Pair<String, String>): urlbuilder = this / mapOf(other)

    private companion object {
        /**
         * if the [port] is ommitted, determines the default based on the [scheme]
         */
        private fun defaultPort(scheme: Scheme) = if (scheme == Scheme.http) 80 else 443
    }
}

/**
 * an immutable URL
 */
public data class URL(
    val scheme: Scheme,
    val auth: authentication? = null,
    val host: String,
    val port: Int,
    val path: path = path(),
    val params: queryparams = queryparams()
    //TODO: fragments
) {
    override fun toString(): String = "$scheme://${auth ?: ""}$host:$port$path$params"
}

/**
 * encodes a segment to be valid in a URL
 */
internal expect fun encodeURLsegment(segment: String): String