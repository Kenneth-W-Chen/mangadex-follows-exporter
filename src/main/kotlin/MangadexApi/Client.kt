package MangadexApi

import MangadexApi.Exceptions.*
import MangadexApi.Data.TokenInfo

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.HttpRequestBuilder

class Client(
    private val username: String,
    private val password: String,
    private val clientId: String,
    private val clientSecret: String
) {
    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    private val httpClient: HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(ContentNegotiation) {
            json()
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(accessToken, refreshToken)
                }

                refreshTokens {
                    val formParameters: Parameters = Parameters.build {
                        append("grant_type", "refresh_token")
                        append("refresh_token", refreshToken)
                        append("client_id", clientId)
                        append("client_secret", clientSecret)
                    }
                    oldTokens
                    val response: HttpResponse = httpClient.submitForm(
                        "https://auth.mangadex.org/realms/mangadex/protocol/openid-connect/token",
                        formParameters,
                        false
                    ) { markAsRefreshTokenRequest() }
                    if (response.status != HttpStatusCode.OK) {
                        throw UnexpectedResponseException("API returned ${response.status} when trying to fetch a token. Body:\n${response.body<String>()}")
                    }
                    val body = response.body<TokenInfo>()
                    BearerTokens(body.accessToken, body.refreshToken)
                }
                sendWithoutRequest {
                    request-> urlRequiresAuth(request)
                }
            }
        }
    }


    suspend fun fetchTokens() {
        val formParameters: Parameters = Parameters.build {
            append("grant_type", "password")
            append("username", username)
            append("password", password)
            append("client_id", clientId)
            append("client_secret", clientSecret)
        }
        val response: HttpResponse = httpClient.submitForm(
            "https://auth.mangadex.org/realms/mangadex/protocol/openid-connect/token",
            formParameters,
            false
        )

        if (response.status == HttpStatusCode.Unauthorized) {
            throw InvalidUserCredentialsException()
        } else if (response.status != HttpStatusCode.OK) {
            throw UnexpectedResponseException("API returned ${response.status} when trying to fetch a token.")
        }
        val body = response.body<TokenInfo>()
        accessToken = body.accessToken
        refreshToken = body.refreshToken
    }

    suspend fun sendRequest(
        endpoint: String,
        parameters: Parameters,
        requireAuth: Boolean = true,
        method: HttpMethod = HttpMethod.Post,
        contentType: String = "application/json",
        body: Any? = null
    ) {

    }

    suspend fun handleRatelimit(response: HttpResponse) {

    }

    private fun urlRequiresAuth(request: HttpRequestBuilder) : Boolean {
        if(request.url.host == "api.mangadex.org") return false
        if(request.url.pathSegments.isEmpty()) return false
        when(request.url.pathSegments[0]){
            "user" -> return true
        }
        println("Path segments: ${request.url.pathSegments}")
        return false
    }
}
