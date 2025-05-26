package MangaUpdatesAPI

import MangaUpdatesAPI.Exceptions.InvalidMUCredentialsException
import MangaUpdatesAPI.Exceptions.UnexpectedMUApiResponseException
import MangaUpdatesAPI.ResponseClasses.ListData
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
import kotlinx.serialization.json.JsonObject

class Client(private val username: String, private val password: String) {
    private val apiVersion = "v1"

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

    suspend fun addTitlesToListById(seriesIds: Array<String>, listId: Int) {
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
    }

    suspend fun addTitlesToListByTitle(titles: Array<String>, listId: Int) {
        var body: String = "["
        for(title in titles){
            body += "{ \"series_title\": \"${title.replace("\"","\\\"")} },"
        }
        body = body.removeSuffix(",") + "]"
        val url: Url = buildUrl {
            protocol = URLProtocol.HTTPS
            host = "api.mangaupdates.com"
            path(apiVersion, "lists", listId.toString(), "series", "bulk")
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
    }

    suspend fun getTitleId(pageId: String): String{
        val response = client.get{
            url{
                protocol = URLProtocol.HTTPS
                host = "www.mangaupdates.com"
                path("series.html")
                parameter("id", pageId)
            }
        }
        if(!response.status.isSuccess()){
            throw UnexpectedMUApiResponseException("Unexpected response from server when fetching title ID by title: ${response.status}: ${response.body<String>()}")
        }
        return Regex("href=\"https:\\/\\/api.mangaupdates.com\\/v1\\/series\\/([0-9]+)\\/rss\">").find(response.body<String>())!!.groupValues[1]
    }

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

    private fun urlRequiresAuth(url: URLBuilder): Boolean{
        if(url.host != "api.mangaupdates.com") return false
        if(url.pathSegments.isEmpty() || url.pathSegments.size < 2) return false
        if(url.pathSegments.size == 3 && url.pathSegments[1] == "account" && url.pathSegments[2] == "login") return false
        return true
    }
}