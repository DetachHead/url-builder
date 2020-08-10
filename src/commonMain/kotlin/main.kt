enum class Scheme { http, https }

/**
 * an authentication that goes before the host in a [URL]
 */
data class authentication(val username: String, val password: String) {
    override fun toString() = "${encodeURLsegment(username)}:${encodeURLsegment(password)}@"
}

data class path(val segments: List<String>) : List<String> by segments {
    constructor(vararg segments: String) : this(segments.asList())

    override fun toString() =
        (if (segments.isNotEmpty()) "/" else "") + segments.joinToString("/") { encodeURLsegment(it) }
}

/**
 * [URL] query parameters
 */
data class queryparams(val value: Map<String, String> = mapOf()) : Map<String, String> by value {
    override fun toString() =
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
class urlbuilder(
    var scheme: Scheme,
    var auth: authentication? = null,
    var host: String,
    var port: Int = defaultPort(scheme),
    block: urlBuilderBlock = {}
) {
    /**
     * constructs a [urlbuilder] without an [auth]
     */
    constructor(scheme: Scheme, host: String, port: Int = defaultPort(scheme), block: urlBuilderBlock = {}) :
            this(scheme, null, host, port, block)

    /**
     * constructs a [urlbuilder] with a [Pair] of [String]s as the [authentication]
     */
    constructor(
        scheme: Scheme,
        auth: Pair<String, String>,
        host: String,
        port: Int = defaultPort(scheme),
        block: urlBuilderBlock = {}
    ) : this(scheme, authentication(auth.first, auth.second), host, port, block)

    var path = path()
    var params = queryparams()

    init {
        @Suppress("unused_expression") //https://youtrack.jetbrains.com/issue/KT-21282
        block()
    }

    /**
     * builds an immutable [URL] from the properties in the current [urlbuilder]
     */
    fun build() = URL(scheme, auth, host, port, path, params)

    override fun toString() = build().toString()

    /**
     * adds the two [String]s to the [path]
     */
    operator fun String.div(other: String) = this@urlbuilder.apply { this / this@div / other }

    /**
     * adds this [String] and the given [Map] of parameters to the [urlbuilder]
     */
    operator fun String.div(other: Map<String, String>) = this@urlbuilder.apply { this / this@div / other }

    /**
     * adds this [String] and the given [Pair] of parameters to the [urlbuilder]
     */
    operator fun String.div(other: Pair<String, String>) = this / mapOf(other)

    /**
     * adds the given [String] to the [path]
     */
    operator fun div(other: String) = apply { path = path(path + other) }

    /**
     * takes a [Map] of [String]s and adds them to the [params] (replaces params if theyre already there)
     */
    operator fun div(other: Map<String, String>) = apply { params = queryparams(params + other) }

    /**
     * takes a [Pair] of [String]s and adds them to the [params] (replaces the param if its already there)
     */
    operator fun div(other: Pair<String, String>) = this / mapOf(other)

    companion object {
        /**
         * if the [port] is ommitted, determines the default based on the [scheme]
         */
        private fun defaultPort(scheme: Scheme) = if (scheme == Scheme.http) 80 else 443
    }
}

/**
 * an immutable URL
 */
data class URL(
    val scheme: Scheme,
    val auth: authentication? = null,
    val host: String,
    val port: Int,
    var path: path = path(),
    var params: queryparams = queryparams()
    //TODO: fragments
) {
    override fun toString() = "$scheme://${auth ?: ""}$host:$port$path$params"
}

/**
 * encodes a segment to be valid in a URL
 */
expect fun encodeURLsegment(segment: String): String