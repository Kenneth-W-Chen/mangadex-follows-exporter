import MangadexApi.Client
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.Properties

suspend fun main() {
//    println(Paths.get("").toAbsolutePath().toString())
    val secrets: Properties = Properties()
    secrets.load(FileInputStream("secrets.properties"))
//    val client:Client = Client()
//    client.fetchToken()
}