enum class Scheme { http, https }

data class authentication(val username: String, val password: String) {
    override fun toString() = "$username:$password"
}

typealias Param = Pair<String, String>
typealias Params = Map<String, String>
typealias urlBuilderBlock = urlbuilder.() -> Unit

class urlbuilder(
    private val scheme: Scheme,
    auth: authentication? = null,
    host: String,
    port: Int = defaultPort(scheme),
    block: urlBuilderBlock = {}
) {
    constructor(scheme: Scheme, host: String, port: Int = defaultPort(scheme), block: urlBuilderBlock = {}) :
            this(scheme, null, host, port, block)

    val url = url(scheme, auth, host, port)

    init {
        block()
    }

    override fun toString() = url.toString()

    operator fun String.div(other: String) = url.apply { path += listOf(this@div, other) }

    operator fun url.div(other: String) = url.apply { path += other }

    operator fun url.div(other: Params) = url.apply { params += other }

    operator fun url.div(other: Param) = this / mapOf(other)

    fun params(params: Params) = url.apply { this.params = params }

    fun params(vararg params: Param) = params(params.toMap())

    companion object {
        private fun defaultPort(scheme: Scheme) = if (scheme == Scheme.http) 80 else 443
    }
}

data class url(
    val scheme: Scheme,
    val auth: authentication? = null,
    val host: String,
    val port: Int,
    var path: List<String> = listOf(),
    var params: Params = mapOf()
) {
    override fun toString() = listOfNotNull(
        "$scheme://${auth?.let { "$it@" } ?: ""}$host:$port", *path.toTypedArray()).joinToString("/") +
            params.let { if (it.isNotEmpty()) it.entries.joinToString("&", "?") { "${it.key}=${it.value}" } else null }

}