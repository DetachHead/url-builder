enum class Scheme { http, https }

data class authentication(val username: String, val password: String)

typealias Param = Pair<String, String>
typealias Params = Map<String, String>
typealias urlBuilderBlock = (urlbuilder.() -> Unit)

class urlbuilder(
    val scheme: Scheme,
    val auth: authentication? = null,
    val host: String,
    val port: Int = defaultPort(scheme),
    val block: urlBuilderBlock? = null
) {
    constructor(scheme: Scheme, host: String, port: Int = defaultPort(scheme), block: urlBuilderBlock? = null) :
            this(scheme, null, host, port, block)

    val url = url(scheme, auth, host, port)

    operator fun String.div(other: String) = url.apply { path += this@div + other }

    operator fun url.div(other: String) = url.apply { path += other }

    operator fun url.div(other: Params) = url.apply {TODO()}

    fun params(params: Params) = url.apply { queryParams = params }

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
    var queryParams: Params = mapOf()
)