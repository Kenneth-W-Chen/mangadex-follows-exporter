package MangadexApi
import MangadexApi.Exceptions.*
import MangadexApi.Data.TokenResponse

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json

class Client(
    private val username: String,
    private val password: String,
    private val clientId: String,
    private val clientSecret: String
) {
    private val httpClient: HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(ContentNegotiation){
            json()
        }
    }

    private lateinit var token: String
    private lateinit var refreshToken: String

    suspend fun fetchToken() {
        val formParameters: Parameters = Parameters.build {
            append("grant_type", "password")
            append("username", username)
            append("password", password)
            append("client_id", clientId)
            append("client_secret", clientSecret)
        }
        val response: HttpResponse = httpClient.submitForm("https://auth.mangadex.org/realms/mangadex/protocol/openid-connect/token", formParameters, false)
        if(response.status == HttpStatusCode.Unauthorized) {
            throw InvalidUserCredentialsException()
        } else if(response.status != HttpStatusCode.OK){
            throw UnexpectedResponseException("API returned ${response.status} when trying to fetch a token.")
        }
        val body = response.body<TokenResponse>()
        println(body)
        token = body.accessToken
        refreshToken = body.refreshToken

    }

    suspend fun refreshToken() {
        val formParameters: Parameters = Parameters.build {
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
            append("client_id", clientId)
            append("client_secret", clientSecret)
        }
    }
}
