enum class Scheme { http, https }

data class authentication(val username: String, val password: String) {
    override fun toString() = "$username:$password@"
}

data class queryparams(val value: Map<String, String> = mapOf()) : Map<String, String> by value {
    override fun toString() =
        value.let { if (it.isNotEmpty()) it.entries.joinToString("&", "?") { "${it.key}=${it.value}" } else "" }
}

private typealias urlBuilderBlock = urlbuilder.() -> Unit

class urlbuilder(
    var scheme: Scheme,
    var auth: authentication? = null,
    var host: String,
    var port: Int = defaultPort(scheme),
    block: urlBuilderBlock = {}
) {
    constructor(scheme: Scheme, host: String, port: Int = defaultPort(scheme), block: urlBuilderBlock = {}) :
            this(scheme, null, host, port, block)

    constructor(
        scheme: Scheme,
        auth: Pair<String, String>,
        host: String,
        port: Int = defaultPort(scheme),
        block: urlBuilderBlock = {}
    ) : this(scheme, authentication(auth.first, auth.second), host, port, block)

    var path = mutableListOf<String>()
    var params = queryparams()

    init {
        @Suppress("unused_expression") //https://youtrack.jetbrains.com/issue/KT-21282
        block()
    }

    fun build() = URL(scheme, auth, host, port, path, params)

    override fun toString() = build().toString()

    operator fun String.div(other: String) = this@urlbuilder.apply { this / this@div / other }

    operator fun div(other: String) = apply { path.add(other) }

    operator fun div(other: Map<String, String>) = apply { params = queryparams(params + other) }

    operator fun div(other: Pair<String, String>) = this / mapOf(other)

    companion object {
        private fun defaultPort(scheme: Scheme) = if (scheme == Scheme.http) 80 else 443
    }
}

data class URL(
    val scheme: Scheme,
    val auth: authentication? = null,
    val host: String,
    val port: Int,
    var path: List<String> = listOf(),
    var params: queryparams = queryparams()
) {
    override fun toString() = listOfNotNull(
        "$scheme://${auth ?: ""}$host:$port", *path.toTypedArray()
    ).joinToString("/") +
            params
}