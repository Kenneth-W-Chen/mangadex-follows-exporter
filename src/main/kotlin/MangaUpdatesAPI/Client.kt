package MangaUpdatesAPI

import MangaUpdatesAPI.Exceptions.InvalidMUCredentialsException
import MangaUpdatesAPI.Exceptions.UnexpectedMUApiResponseException
import MangaUpdatesAPI.ResponseClasses.CreateCustomListResponse
import MangaUpdatesAPI.ResponseClasses.ListData
import MangaUpdatesAPI.ResponseClasses.ListType
import MangaUpdatesAPI.ResponseClasses.LoginResponseData
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

/**
 * Class to interact with MangaUpdates' API.
 * @param username The MangaUpdates user's username.
 * @param password The MangaUpdates user's password.
 */
class Client(private val username: String, private val password: String) {
    /**
     * MangaUpdates API version. This was initially made and tested with v1.
     */
    private val apiVersion = "v1"

    /**
     * Object to perform HTTP requests.
     */
    private var client: HttpClient = HttpClient(){
        expectSuccess = false
        install(Auth){
            bearer{
                loadTokens { fetchToken() }

                refreshTokens { fetchToken() }

                sendWithoutRequest{ request -> urlRequiresAuth(request.url)}
            }
        }
        install(ContentNegotiation){
            json()
        }
    }

    /**
     * Fetches the user's reading lists.
     * @return List of [ListData] representing the user's lists.
     * @throws UnexpectedMUApiResponseException Thrown if the server doesn't respond with a success code.
     */
    suspend fun fetchLists(): List<ListData> {
        val response: HttpResponse = client.get{
            url{
                protocol = URLProtocol.HTTPS
                host = "api.mangaupdates.com"
                path(apiVersion, "lists")
            }
        }
        if(!response.status.isSuccess()){
            throw UnexpectedMUApiResponseException("Unexpected response from server when fetching lists - ${response.status}: ${response.body<JsonObject>()["reason"]}")
        }
        return response.body<List<ListData>>()
    }

    /**
     * Adds series to a list by their ID. Will repeat in 5-second increments if the server returns HTTP412.
     * @param seriesIds List of IDs to add.
     * @param listId The reading list to add the series to.
     * @return The response from the server. Useful for diagnosing partial successes.
     * @throws UnexpectedMUApiResponseException Thrown if the server doesn't respond with a success code.
     */
    suspend fun addTitlesToListById(seriesIds: MutableList<String>, listId: String): HttpResponse {
        var body: String = "["
        for(seriesId in seriesIds){
            body += "{" +
                        "\"series\":{" +
                            "\"id\":    $seriesId" +
                        "}," +
                        "\"list_id\":   $listId" +
                    "},"
        }
        body = body.removeSuffix(",")
        body += "]"


        var response: HttpResponse = client.post{
            url{
                protocol = URLProtocol.HTTPS
                host = "api.mangaupdates.com"
                path(apiVersion, "lists", "series")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        while(response.status == HttpStatusCode.PreconditionFailed){
            delay(5000)
            response = client.post{
                url{
                    protocol = URLProtocol.HTTPS
                    host = "api.mangaupdates.com"
                    path(apiVersion, "lists", "series")
                }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if(!response.status.isSuccess()){
            throw UnexpectedMUApiResponseException("Unexpected response from server when adding series - ${response.status}: ${response.body<JsonObject>()["reason"]}. Full response: ${response.body<JsonObject>()}")
        }
        return response
    }

    /**
     * Adds series to a list by their ID. Will repeat in 5-second increments if the server returns HTTP412.
     * @param titles Title of the series to add.
     * @param listId The reading list to add the series to.
     * @return The response from the server. Useful for diagnosing partial successes.
     * @throws UnexpectedMUApiResponseException Thrown if the server doesn't respond with a success code.
     */
    suspend fun addTitlesToListByTitle(titles: List<String>, listId: String): HttpResponse {
        val body: JsonArray = buildJsonArray {
            for(title in titles){
                add(buildJsonObject {
                    put("priority", "High")
                    put("series_title", title)
                })
            }
        }

        val url: Url = buildUrl {
            protocol = URLProtocol.HTTPS
            host = "api.mangaupdates.com"
            path(apiVersion, "lists", listId, "series", "bulk")
        }
        var response: HttpResponse = client.post(url){
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        while(response.status == HttpStatusCode.PreconditionFailed){
            delay(5000)
            response = client.post(url){
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

        if(!response.status.isSuccess()){
            throw UnexpectedMUApiResponseException("Unexpected response from server when adding series - ${response.status}: ${response.body<JsonObject>()["reason"]}. Full response: ${response.body<JsonObject>()}")
        }
        return response
    }

    /**
     * Gets a series' ID from the page ID. This function only exists because MangaDex stores the wrong ID.
     * Because this fetches the actual series page (and therefore fetches a lot of data), this probably shouldn't be used without client rate limiting.
     * @param pageId The page ID i.e., https://www.mangaupdates.com/series/{pageId}/ or https://www.mangaupdates.com/series.html?id={pageId}.
     * @return The actual ID.
     * @throws UnexpectedMUApiResponseException Thrown if the server doesn't respond with a success code.
     */
    suspend fun getTitleId(pageId: String): String{
        var response = client.get{
            url{
                protocol = URLProtocol.HTTPS
                host = "www.mangaupdates.com"
                appendPathSegments("series", pageId.trim{c -> c == '"' || c.isWhitespace()})
            }
        }
        while(response.status == HttpStatusCode.PreconditionFailed){
            delay(5000)
            response = client.get{
                url{
                    protocol = URLProtocol.HTTPS
                    host = "www.mangaupdates.com"
                    appendPathSegments("series", pageId.trim{c -> c == '"' || c.isWhitespace()})
                }
            }
        }
        if(response.status == HttpStatusCode.NotFound){
            response = client.get{
                url{
                    protocol = URLProtocol.HTTPS
                    host = "www.mangaupdates.com"
                    path("series.html")
                    parameter("id", pageId.trim{c -> c == '"' || c.isWhitespace()})
                }
            }
            while(response.status == HttpStatusCode.PreconditionFailed){
                delay(5000)
                response = client.get{
                    url{
                        protocol = URLProtocol.HTTPS
                        host = "www.mangaupdates.com"
                        path("series.html")
                        parameter("id", pageId.trim{c -> c == '"' || c.isWhitespace()})
                    }
                }
            }
        }
        if(!response.status.isSuccess()){
            throw UnexpectedMUApiResponseException("Unexpected response from server when fetching series ID by page ID ($pageId): ${response.status}: ${response.body<String>()}")
        }
        return try {
            Regex("href=\"https:\\/\\/api.mangaupdates.com\\/v1\\/series\\/([0-9]+)\\/rss\">").find(response.body<String>())!!.groupValues[1]
        } catch(e: NullPointerException){
            ""
        }
    }

    /**
     * Creates a reading list.
     * @param title The title of the new list.
     * @param description The description of the new list.
     * @param type The type of list. See [ListType].
     * @return The list's ID.
     * @throws UnexpectedMUApiResponseException Thrown if the server doesn't respond with a success code.
     */
    suspend fun makeList(title: String, description: String, type: ListType): String{
       val url = buildUrl{
            protocol = URLProtocol.HTTPS
            host = "api.mangaupdates.com"
            path(apiVersion, "lists")
        }
        val body = buildJsonObject{
            put("title", title)
            put("description", description)
            put("type", Json.encodeToJsonElement(type))
        }
        var response = client.post(url){
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        while(response.status == HttpStatusCode.PreconditionFailed){
            delay(5000)
            response = client.post(url){
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if(!response.status.isSuccess()){
            throw UnexpectedMUApiResponseException("Unexpected response from server when making a new reading list: ${response.status}: ${response.body<String>()}")
        }
        return response.body<CreateCustomListResponse>().context.id
    }

    /**
     * Returns the list ID with the given title. If the list doesn't exist, creates the list and returns the new list's ID.
     * @param title The title of the list whose ID will be returned.
     * @param description The description of the list, if a new list will be created. Defaults to null.
     * @return The list's ID.
     */
    suspend fun getListId(title: String, description: String? = null): String{
        val readingLists = fetchLists()
        val mdList = readingLists.firstOrNull { it.title == title }
        var readingListId: String = ""
        if(mdList!=null){
            readingListId = mdList.listId
        } else{
            readingListId = makeList(title, description.toString(), ListType.READ)
        }
        return readingListId
    }

    /**
     * Fetches a session token for the API client.
     */
    private suspend fun fetchToken(): BearerTokens {
        var response: HttpResponse
        try {
            response = client.put {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.mangaupdates.com"
                    path(apiVersion, "account", "login")
                }
                contentType(ContentType.Application.Json)
                setBody("{\"username\":\"$username\",\"password\":\"$password\"}")
            }
        } catch(e: UninitializedPropertyAccessException){
            throw InvalidMUCredentialsException()
        }
        val responseBody = response.body<LoginResponseData>()
        if(response.status != HttpStatusCode.OK) throw UnexpectedMUApiResponseException("Unexpected response from server when fetching session token - ${response.status}: ${responseBody.reason}")
        return BearerTokens(responseBody.context!!.sessionToken, null)
    }

    /**
     * Function to determine if an endpoint needs auth headers. Used exclusively for [client].
     */
    private fun urlRequiresAuth(url: URLBuilder): Boolean{
        if(url.host != "api.mangaupdates.com") return false
        if(url.pathSegments.isEmpty() || url.pathSegments.size < 2) return false
        if(url.pathSegments.size == 3 && url.pathSegments[1] == "account" && url.pathSegments[2] == "login") return false
        return true
    }
}