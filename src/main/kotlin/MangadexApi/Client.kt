package MangadexApi

import MangadexApi.Data.MangaInfoResponse
import MangadexApi.Data.SimplifiedMangaInfo
import MangadexApi.Exceptions.*
import MangadexApi.Data.TokenInfo

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import javax.swing.JTextPane

/**
 * Class to interact with MangaDex's API.
 * @param username The MangaDex username.
 * @param password The MangaDex password.
 * @param clientId The MangaDex API client ID.
 * @param clientSecret The MangaDex API client secret.
 */
class Client(
    private val username: String,
    private val password: String,
    private val clientId: String,
    private val clientSecret: String
) {
    /**
     * MangaDex API access token.
     */
    private lateinit var accessToken: String

    /**
     * MangaDex API refresh token.
     */
    private lateinit var refreshToken: String

    /**
     * Object to perform HTTP requests.
     */
    private val httpClient: HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(ContentNegotiation) {
            json()
        }
        install(Auth) {
            bearer {
                loadTokens {
                    fetchTokens()
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
                        throw UnexpectedMDApiResponseException("API returned ${response.status} when trying to fetch a token. Body:\n${response.body<String>()}")
                    }
                    val body = response.body<TokenInfo>()
                    BearerTokens(body.accessToken, body.refreshToken)
                }
                sendWithoutRequest { request ->
                    urlRequiresAuth(request)
                }
            }
        }
    }

    /**
     * Fetches an authorization token for the client.
     */
    private suspend fun fetchTokens(): BearerTokens {
        val formParameters: Parameters = Parameters.build {
            append("grant_type", "password")
            append("username", username)
            append("password", password)
            append("client_id", clientId)
            append("client_secret", clientSecret)
        }
        var response: HttpResponse
        try {
            response = httpClient.submitForm(
                "https://auth.mangadex.org/realms/mangadex/protocol/openid-connect/token",
                formParameters,
                false
            )
        } catch(e:UninitializedPropertyAccessException) {
            throw InvalidMDUserCredentialsException()
        }

        if (response.status != HttpStatusCode.OK) {
            throw UnexpectedMDApiResponseException("API returned ${response.status} when trying to fetch a token.")
        }
        val body = response.body<TokenInfo>()
        return BearerTokens(body.accessToken, body.refreshToken)
    }


    /**
     * Fetches manga from the user's [followed list](https://mangadex.org/titles/follows).
     * @param limit Sets the max number of titles to fetch. The API will return at most this amount. Default 10; Max 100 (API limitation).
     * @param offset The initial index to start fetching from.
     * @return The full [HttpResponse] from the API.
     */
    suspend fun getFollowedMangaList(limit: Int = 10, offset: Int = 0): HttpResponse {
        return httpClient.get() {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.mangadex.org"
                pathSegments = listOf("user", "follows", "manga")
                parameters.append("limit", limit.toString())
                parameters.append("offset", offset.toString())
            }
        }
    }

    /**
     * Returns all followed manga for the user.
     * @param perFetchLimit Sets the limit of titles to fetch per API call. The API may send fewer titles in a single response, even if more exist. Max 100 (API limitation).
     * @param initialOffset The initial index to start fetching from.
     * @param localePreference The title to return for each [SimplifiedMangaInfo].
     * @return A list of [SimplifiedMangaInfo] containing all titles fetched.
     */
    suspend fun getAllFollowedManga(perFetchLimit: Int = 100, initialOffset: Int = 0, localePreference: Array<String> = arrayOf("ja","ja-ro","ko","ko-ro","zh","zh-hk","zh-ro","en"),): MutableList<SimplifiedMangaInfo> {
        //assume global ratelimit is 1 request/sec (I'm told 5)
        // pause 1 second before starting in case another request was already sent
        delay(1000)
        var currentOffset: Int = initialOffset
        var expectedTotal: Int = 99
        var emptyDataReturned: Boolean = false
        var stepCount: Int = 1  // purely statistical number... non-essential
        var mangaList: MutableList<SimplifiedMangaInfo> = mutableListOf()
        do {
            for (i in 1..5) {
                println("Current index: $currentOffset\n")
                var response: HttpResponse = getFollowedMangaList(perFetchLimit, currentOffset)
                if (response.status == HttpStatusCode.TooManyRequests) {
                    println("Reached ratelimit. Response headers: \n" + response.headers.toString() + "\n")
                    try {
                        var currentPeriodEnd: Int = (response.headers.get("RateLimit-Retry-After") ?: response.headers.get("X-RateLimit-Retry-After") ?: "60000").toInt()
                        val waitTime = System.currentTimeMillis() - currentPeriodEnd + 1
                        println("Waiting $waitTime milliseconds.\n")
                        delay(waitTime)
                    } catch (e: NumberFormatException) {
                        delay(60000)
                        println("Waiting 60000 milliseconds.\n")
                    }
                } else if (response.status.isSuccess()) {
                    val responseBody = response.body<MangaInfoResponse>()
                    emptyDataReturned = responseBody.data.isEmpty()
                    expectedTotal = responseBody.total
                    for(manga in responseBody.data){
                        mangaList.add(manga.toSimplifiedMangaInfo(localePreference))
                    }
                    println("Successful response ($stepCount): Received " + responseBody.data.size + " titles.\n")
                    currentOffset += responseBody.data.size
                    stepCount++

                } else {
                    println("Unexpected HTTP Response: ${response.status}")
                }

                if(currentOffset >= expectedTotal) {
                    break
                }
            }

            delay(1000)
        } while (currentOffset <= expectedTotal && !emptyDataReturned)

        return mangaList
    }

    /**
     * Function to determine if an endpoint needs auth headers. Used exclusively for [httpClient].
     */
    private fun urlRequiresAuth(request: HttpRequestBuilder): Boolean {
        if (request.url.host == "auth.mangadex.org") return false
        if (request.url.pathSegments.isEmpty()) return false
        when (request.url.pathSegments[0]) {
            "user" -> return true
        }
        return false
    }
}
