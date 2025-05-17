import MangadexApi.Client
import MangadexApi.Data.MangaInfo
import MangadexApi.Data.MangaInfoResponse
import MangadexApi.Data.SimplifiedMangaInfo
import io.ktor.client.call.body
import io.ktor.client.statement.request
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Properties

suspend fun main() {
    val secrets: Properties = Properties()
    secrets.load(FileInputStream("secrets.properties"))
    val client:Client = Client(secrets.getProperty("username"), secrets.getProperty("password"), secrets.getProperty("client-id"), secrets.getProperty("client-secret"))
    client.fetchTokens()
    var list = client.getAllFollowedManga()
    val config: Properties = Properties()
    config.load(FileInputStream("config.properties"))
    val saveLinks: List<Links> = config.getProperty("links").split(",").map({ Links.valueOf(it) })
    writeToFile(list, "My_MangaDex_Follows", saveLinks)
}
enum class Links{
    al,
    ap,
    bw,
    mu,
    nu,
    kt,
    amz,
    ebj,
    mal,
    cdj,
    raw,
    engtl,
}

enum class BufferingMode{
    PER_TITLE,
    PER_LIST
}

fun writeToFile(mangaList:MutableList<SimplifiedMangaInfo>, fileName: String = "My_MangaDex_Follows", saveLinks: List<Links> = listOf(Links.mu), bufferingMode: BufferingMode=BufferingMode.PER_TITLE) {
    val fileNameEnd: String = "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".txt"
    var titlesFile: File = File(fileName + "_" +"Titles" + fileNameEnd)
    var linksFiles: Map<Links, File> = saveLinks.associateBy({it}, { File(fileName + "_" +it.name + fileNameEnd)})

    //stats
    var titlesAdded: Int = 0
    var nullLinks: MutableMap<Links, Int> = saveLinks.associateBy({it}, {0}).toMutableMap()

    titlesFile.createNewFile()
    linksFiles.forEach { it.value.createNewFile() }
    when(bufferingMode) {
        BufferingMode.PER_TITLE -> {
            for(manga in mangaList){
                titlesFile.appendText(manga.title + "\n")
                for(link in saveLinks){
                    val _link = manga.links?.get(link.name)
                    if(_link == null) nullLinks[link] = nullLinks[link]!!.plus(1)
                    linksFiles[link]!!.appendText(_link.toString() + "\n")
                }
                titlesAdded++
            }
        }
        BufferingMode.PER_LIST -> {
            var titles: String = ""
            val links: MutableMap<Links, String> = saveLinks.associateBy({it}, {""}).toMutableMap()
            for(manga in mangaList){
                titles += manga.title + "\n"
                titlesAdded++
                for(link in saveLinks){
                    val _link = manga.links?.get(link.name)
                    if(_link == null) nullLinks[link] = nullLinks[link]!!.plus(1)
                    links[link] = links[link] + _link.toString() + "\n"
                }
            }
            titlesFile.appendText(titles)
            for(link in saveLinks){
                linksFiles[link]!!.appendText(links[link]!!)
            }
        }
    }

    var statsFile = File(fileName + "_stats" + fileNameEnd)
    statsFile.createNewFile()
    statsFile.writeText("Titles added: $titlesAdded\nNull links count:\n")
    for(link in saveLinks){
        statsFile.appendText("\t" + link.name + ":\t" + nullLinks[link]!!.toString())
    }

}