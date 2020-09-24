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

private typealias urlBuilderBlock = URLbuilder.() -> Unit

/**
 * builds a [URL]
 */
public class URLbuilder(
    public var scheme: Scheme,
    public var auth: authentication? = null,
    public var host: String,
    public var port: Int = defaultPort(scheme),
    block: urlBuilderBlock = {}
) {
    /**
     * constructs a [URLbuilder] without an [auth]
     */
    public constructor(scheme: Scheme, host: String, port: Int = defaultPort(scheme), block: urlBuilderBlock = {}) :
            this(scheme, null, host, port, block)

    /**
     * constructs a [URLbuilder] with a [Pair] of [String]s as the [authentication]
     */
    public constructor(
        scheme: Scheme,
        auth: Pair<String, String>,
        host: String,
        port: Int = defaultPort(scheme),
        block: urlBuilderBlock = {}
    ) : this(scheme, authentication(auth.first, auth.second), host, port, block)

    private var path: path = path()
    private var params: queryparams = queryparams()
    private var fragment: String? = null

    init {
        @Suppress("unused_expression") //https://youtrack.jetbrains.com/issue/KT-21282
        block()
    }

    /**
     * builds an immutable [URL] from the properties in the current [URLbuilder]
     */
    private fun build() = URL(scheme, auth, host, port, path, params, fragment)

    override fun toString(): String = build().toString()

    /**
     * adds the two [String]s to the [path]
     */
    public operator fun String.div(other: String): URLbuilder = this@URLbuilder.apply { this / this@div / other }

    /**
     * adds the given [String] to the [path]
     */
    public operator fun div(other: String): URLbuilder = apply { path = path(path + other) }

    /**
     * adds this [String] and the given [Map] of parameters to the [URLbuilder]
     */
    public infix fun String.params(other: Map<String, String>): URLbuilder =
        this@URLbuilder.apply { this / this@params params other }

    /**
     * adds this [String] and the given [Pair] of parameters to the [URLbuilder]
     */
    public infix fun String.params(other: Pair<String, String>): URLbuilder = this params mapOf(other)

    /**
     * takes a [Map] of [String]s and adds them to the [params] (replaces params if theyre already there)
     */
    public infix fun params(other: Map<String, String>): URLbuilder = apply { params = queryparams(params + other) }

    /**
     * takes a [Pair] of [String]s and adds them to the [params] (replaces the param if its already there)
     */
    public infix fun params(other: Pair<String, String>): URLbuilder = this params mapOf(other)
    /**
     * takes a [Pair] of [String]s and adds them to the [params] (replaces the param if its already there)
     */
    public fun params(vararg other: Pair<String, String>): URLbuilder = this params other.toMap()

    /** adds the given [String] as a fragment to the end of the [URLbuilder] */
    public infix fun frag(fragment: String): URLbuilder = apply { this.fragment = fragment }

    /** adds the given [String] as a fragment to the end of the [URLbuilder] */
    public infix fun String.frag(fragment: String): URLbuilder = this@URLbuilder.apply { this frag fragment }

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
    val params: queryparams = queryparams(),
    val fragment: String? = null
) {
    override fun toString(): String = "$scheme://${auth ?: ""}$host:$port$path$params${fragment?.let { "#$it" } ?: ""}"
}

/**
 * encodes a segment to be valid in a URL
 */
internal expect fun encodeURLsegment(segment: String): String