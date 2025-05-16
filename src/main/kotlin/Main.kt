import MangadexApi.Client
import MangadexApi.Data.MangaInfo
import MangadexApi.Data.MangaInfoResponse
import io.ktor.client.call.body
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.Properties

suspend fun main() {
    val secrets: Properties = Properties()
    secrets.load(FileInputStream("secrets.properties"))
    val client:Client = Client(secrets.getProperty("username"), secrets.getProperty("password"), secrets.getProperty("client-id"), secrets.getProperty("client-secret"))
    client.fetchTokens()
    
}