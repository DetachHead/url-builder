package detachhead.urlbuilder

public enum class Scheme { http, https }

/** immutable authentication details for a [URL] */
public open class Authentication(public open val username: String, public open val password: String) {
    override fun toString(): String = "${encodeURLsegment(username)}:${encodeURLsegment(password)}@"
}

/** mutable authentication details for a [URLbuilder] */
public class AuthenticationBuilder(public override var username: String, public override var password: String) :
    Authentication(username, password) {
    public constructor(other: Authentication) : this(other.username, other.password)
}

/** immutable path details for a [URL] */
public open class Path(public open val segments: List<String>) : List<String> by segments {
    public constructor(vararg segments: String) : this(segments.toList())

    override fun toString(): String =
        (if (segments.isNotEmpty()) "/" else "") + segments.joinToString("/") { encodeURLsegment(it) }
}

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE") // TODO: whats up with this
/** mutable path details for a [URLbuilder] */
public class PathBuilder(segments: MutableList<String>) :
    Path(segments),
    MutableList<String> by segments {

    // https://youtrack.jetbrains.com/issue/KT-60978/CanBePrimaryConstructorProperty-warning-conflicts-with-DelegationToVarProperty
    @Suppress("CanBePrimaryConstructorProperty")
    public override var segments: MutableList<String> = segments

    public constructor(vararg segments: String) : this(segments.toMutableList())
    public constructor(other: Path) : this(other.segments.toMutableList())
}

/** immutable query parameters for a [URL] */
public open class QueryParams(public open val value: Map<String, String> = mapOf()) :
    Map<String, String> by value {
    public constructor(vararg value: Pair<String, String>) : this(value.toMap())

    override fun toString(): String = value.let {
        if (it.isNotEmpty()) it.entries.joinToString("&", "?") {
            "${encodeURLsegment(it.key)}=${encodeURLsegment(it.value)}"
        } else ""
    }
}

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE") // TODO: whats up with this
/** mutable query parameters for a [URLbuilder] */
public class QueryParamsBuilder(value: MutableMap<String, String>) :
    QueryParams(value),
    MutableMap<String, String> by value {
    // https://youtrack.jetbrains.com/issue/KT-60978/CanBePrimaryConstructorProperty-warning-conflicts-with-DelegationToVarProperty
    @Suppress("CanBePrimaryConstructorProperty")
    public override var value: MutableMap<String, String> = value

    public constructor(vararg value: Pair<String, String>) : this(value.toMap().toMutableMap())
    public constructor(other: QueryParams) : this(other.value.toMutableMap())
}

private typealias URLbuilderBlock = URLbuilder.() -> Unit

/**
 * mutable builder for a [URL]
 *
 * once
 */
public class URLbuilder private constructor(
    public var scheme: Scheme,
    public var auth: AuthenticationBuilder? = null,
    public var host: String,
    public var port: Int = defaultPort(scheme),
    public var path: PathBuilder = PathBuilder(),
    public var params: QueryParamsBuilder = QueryParamsBuilder(),
    public var fragment: String? = null,
    // the _ is only to allow explicitly calling this constructor. TODO: think up something better
    @Suppress("LocalVariableName")
    block_: URLbuilderBlock = {}
) {
    public constructor(
        scheme: Scheme,
        auth: AuthenticationBuilder?,
        host: String,
        port: Int = defaultPort(scheme),
        block: URLbuilderBlock = {}
    ) :
        this(
            scheme = scheme,
            auth = auth,
            host = host,
            port = port,
            block_ = block
        )

    /**
     * constructs a [URLbuilder] without an [auth]
     */
    public constructor(scheme: Scheme, host: String, port: Int = defaultPort(scheme), block: URLbuilderBlock = {}) :
        this(scheme = scheme, host = host, port = port, block_ = block)

    /**
     * constructs a [URLbuilder] with a [Pair] of [String]s as the [AuthenticationBuilder]
     */
    public constructor(
        scheme: Scheme,
        auth: Pair<String, String>,
        host: String,
        port: Int = defaultPort(scheme),
        block: URLbuilderBlock = {}
    ) : this(
        scheme = scheme,
        auth = AuthenticationBuilder(auth.first, auth.second),
        host = host,
        port = port,
        block = block
    )

    /**
     * constructs a [URLbuilder] with a [Pair] of [String]s as the [AuthenticationBuilder] and the path as a [String]
     */
    public constructor(
        scheme: Scheme,
        auth: Pair<String, String>,
        host: String,
        port: Int,
        pathString: String
    ) : this(
        scheme = scheme,
        auth = AuthenticationBuilder(auth.first, auth.second),
        host = host,
        port = port,
        path = PathBuilder(pathString)
    )

    /**
     * constructs a [URLbuilder] without an [auth] and the path as a [String]
     */
    public constructor(scheme: Scheme, host: String, port: Int, pathString: String) :
        this(scheme = scheme, host = host, port = port, path = PathBuilder(pathString))

    /**
     * constructs a [URLbuilder] with a [Pair] of [String]s as the [AuthenticationBuilder] and the path as a [String]
     */
    public constructor(
        scheme: Scheme,
        auth: Pair<String, String>,
        host: String,
        pathString: String
    ) : this(
        scheme = scheme,
        auth = AuthenticationBuilder(auth.first, auth.second),
        host = host,
        path = PathBuilder(pathString)
    )

    /**
     * constructs a [URLbuilder] without an [auth] and the path as a [String]
     */
    public constructor(scheme: Scheme, host: String, pathString: String) :
        this(scheme = scheme, host = host, path = PathBuilder(pathString))

    /** creates a [UrlBuilder] from an immutable [URL] */
    public constructor(url: URL) : this(
        scheme = url.scheme,
        auth = if (url.auth == null) null else AuthenticationBuilder(url.auth),
        host = url.host,
        port = url.port,
        path = PathBuilder(url.path),
        params = QueryParamsBuilder(url.params),
        fragment = url.fragment
    )

    init {
        @Suppress("unused_expression") // https://youtrack.jetbrains.com/issue/KT-21282
        block_()
    }

    /**
     * builds an immutable [URL] from the properties in the current [URLbuilder]
     */
    public fun build(): URL = URL(scheme, auth, host, port, path, params, fragment)

    override fun toString(): String = build().toString()

    /**
     * adds the two [String]s to the [PathBuilder]
     */
    public operator fun String.div(other: String): URLbuilder = this@URLbuilder.apply { this / this@div / other }

    /**
     * adds the given [String] to the [PathBuilder]
     */
    public operator fun div(other: String): URLbuilder = apply { path = PathBuilder((path + other).toMutableList()) }

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
    public infix fun params(other: Map<String, String>): URLbuilder =
        apply { params = QueryParamsBuilder((params + other).toMutableMap()) }

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
         * if the [port] is omitted, determines the default based on the [scheme]
         */
        private fun defaultPort(scheme: Scheme) = if (scheme == Scheme.http) 80 else 443
    }
}

/**
 * an immutable URL
 */
public data class URL(
    val scheme: Scheme,
    val auth: Authentication? = null,
    val host: String,
    val port: Int,
    val path: Path = Path(),
    val params: QueryParams = QueryParams(),
    val fragment: String? = null
) {
    override fun toString(): String = "$scheme://${auth ?: ""}$host:$port$path$params${fragment?.let { "#$it" } ?: ""}"
}

/**
 * encodes a segment to be valid in a URL
 */
internal expect fun encodeURLsegment(segment: String): String
