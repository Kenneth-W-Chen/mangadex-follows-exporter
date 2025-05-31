package Utilities

import LogType
import MangaUpdatesAPI.Client
import MangaUpdatesAPI.Exceptions.UnexpectedMUApiResponseException
import MangaUpdatesAPI.ResponseClasses.BulkTitleAddResponse
import MangadexApi.Data.SimplifiedMangaInfo
import io.ktor.client.call.body
import kotlinx.coroutines.delay
import java.nio.file.Path
import kotlin.io.path.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.EnumSet
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.io.path.*
import kotlin.math.min

/**
 * Links that MangaDex stores with each series. Enum name is the enum name used by MangaDex.
 * @param canonicalName The actual name of the link (e.g., [Links.amz] is "Amazon").
 */
enum class Links(val canonicalName: String) {
    amz("Amazon"),
    al("AniList"),
    ap("Anime-Planet"),
    bw("Book Walker"),
    cdj("CDJapan"),
    ebj("eBookJapan"),
    kt("Kitsu"),
    mu("MangaUpdates"),
    mal("MyAnimeList"),
    nu("NovelUpdates"),
    engtl("Official English"),
    raw("Raws"),
}


/**
 * Buffering mode for exporting titles to a TXT/CSV. See [exportMangaList] and [writeToTextFile].
 */
enum class BufferingMode{
    /**
     * Writes to the file for each title before reading information for the next title. Use this if you're worried about stack overflow due to insufficient memory.
     */
    PER_TITLE,

    /**
     * Writes to the file once for the entire list. Use this if you don't want the program to repeatedly open and close files.
     */
    PER_LIST
}

/**
 * Where the manga list should be exported to.
 */
enum class ExportOptions{
    /**
     * Exports it to a separate text file for the titles, and each link.
     */
    TXT,

    /**
     * Exports it to a single CSV.
     */
    CSV,

    /**
     * Exports it to MangaUpdates. See [MangaUpdatesAPI.Client.addTitlesToListByTitle] and [MangaUpdatesAPI.Client.addTitlesToListById].
     */
    MANGAUPDATES,
    MYANIMELIST
}

/**
 * The method to import series to MangaUpdates.
 */
enum class MangaUpdatesImportMethod {
    /**
     * Uses MangaUpdates IDs. This method is slow, but is guaranteed to work. It takes about 5 seconds per series. Series that don't have a MangaUpdates link on MangaDex won't be added.
     */
    ID,

    /**
     * Uses series titles as provided by MangaDex. There's no guarantee this will work for every title since MangaUpdates might not have that particular title for the series.
     */
    TITLE
}

/**
 * Exports a list of [SimplifiedMangaInfo] to the specified export options.
 * @param mangaList The list of manga to export.
 * @param fileName The base filename of the files where title information will be stored. The full filename will be "[fileName]_yyyy_MM_dd_HH_mm_ss.{fileformat}". For text files, the type will be appended after "[fileName]_".
 * @param saveLinks The links to save. See [Links].
 * @param exportOptions Which formats to export the list to. See [ExportOptions].
 * @param bufferingMode See [BufferingMode].
 * @param muClient The [MangaUpdatesAPI.Client] to be used if [ExportOptions.MANGAUPDATES] is set.
 * @param publish A function to consume logging information. Used for [MangadexApiClientWorker.publish].
 */
suspend fun exportMangaList(
    mangaList: MutableList<SimplifiedMangaInfo>,
    fileName: String = "My_MangaDex_Follows",
    saveLinks: EnumSet<Links> = EnumSet.allOf(Links::class.java),
    exportOptions: EnumSet<ExportOptions> = EnumSet.allOf(
        ExportOptions::class.java
    ),
    bufferingMode: BufferingMode = BufferingMode.PER_TITLE,
    muClient: Client? = null,
    muImportMethod: MangaUpdatesImportMethod = MangaUpdatesImportMethod.TITLE,
    publish: ((Pair<String, LogType>) -> Unit)? = null
){
    val homeDir: String = System.getProperty("user.home")
    var titlesFile: Path? = null
    var linksFiles: Map<Links, Path>? = null
    var csvFile: Path? = null
    val makeTxt = exportOptions.contains(ExportOptions.TXT)
    val makeCsv = exportOptions.contains(ExportOptions.CSV)
    var titlesAdded = 0
    val nullLinks: MutableMap<Links, Int> = saveLinks.associateBy({it}, {0}).toMutableMap()
    if(makeCsv || makeTxt){
        val fileNameEnd: String = "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
        if(makeTxt){
            titlesFile = Path(homeDir, "${fileName}_$fileNameEnd.txt")
            titlesFile.createFile()
            linksFiles = saveLinks.associateBy({it}, { Path(homeDir, "${fileName}_${it.name}_$fileNameEnd.txt" )})
            linksFiles.forEach { it.value.createFile() }
        }
        if(makeCsv){
            csvFile = Path(homeDir, "${fileName}_$fileNameEnd.csv")
            csvFile.createFile()
        }
    }


    when(bufferingMode){
        BufferingMode.PER_TITLE -> {
            if(makeCsv) csvFile!!.appendText("title,${saveLinks.joinToString(",", transform = { it.name })}\n")
            for(manga in mangaList){
                if(makeTxt) titlesFile!!.appendText(manga.title + "\n")
                var csvLine = ""
                if(makeCsv) csvLine += "${manga.title},"
                for(link in saveLinks){
                    val _link = manga.links?.get(link.name)
                    if(_link == null) nullLinks[link] = nullLinks[link]!!.plus(1)
                    if(makeTxt) linksFiles!![link]!!.appendText("${_link.toString()}\n")
                    if(makeCsv) csvLine+="${_link.toString()},"
                }
                if(makeCsv) csvFile!!.appendText("$csvLine\n")
                titlesAdded++
            }
        }
        BufferingMode.PER_LIST -> {
            var titles = ""
            var links: MutableMap<Links, String>? = null
            if(makeTxt){
                links = saveLinks.associateBy({it}, {""}).toMutableMap()
            }
            var csvLines = ""
            if(makeCsv) csvLines = "title,${saveLinks.joinToString(",", transform = { it.name })}\n"
            for(manga in mangaList){
                if(makeTxt) titles+=manga.title + "\n"
                if(makeCsv) csvLines += "${manga.title},"
                for(link in saveLinks){
                    val _link = manga.links?.get(link.name)
                    if(_link == null) nullLinks[link] = nullLinks[link]!!.plus(1)

                    if(makeTxt) links!![link] = links[link] + _link.toString() + "\n"
                    if(makeCsv) csvLines+="${_link.toString()},"
                }
                if(makeCsv) csvLines += "\n"
                titlesAdded++
            }
            if(makeTxt){
                titlesFile!!.appendText(titles)
                for(link in saveLinks){
                    linksFiles!![link]!!.appendText(links!![link]!!)
                }
            }
            if(makeCsv) csvFile!!.appendText(csvLines)
        }
    }
    if(exportOptions.contains(ExportOptions.MANGAUPDATES)) {
        when(muImportMethod){
            MangaUpdatesImportMethod.ID -> importToMangaUpdatesByID(muClient!!, mangaList, publish)
            MangaUpdatesImportMethod.TITLE -> importToMangaUpdatesByTitle(muClient!!, mangaList, publish)
        }
    }
    if(exportOptions.contains(ExportOptions.MYANIMELIST)) {
        createMALFile(mangaList,fileName,publish)
    }
}

/**
 * Exports a list of [SimplifiedMangaInfo] to some text files. Essentially [exportMangaList] but without CSVs or MangaUpdates.
 * @param mangaList The list of manga to export.
 * @param fileName The base filename of the files where title information will be stored. The full filename will be "[fileName]_{Type}_yyyy_MM_dd_HH_mm_ss.txt", where "Type" will be "Titles" or one of [Links.name].
 * @param saveLinks The links to save. See [Links].
 * @param bufferingMode See [BufferingMode].
 */
fun writeToTextFile(mangaList:MutableList<SimplifiedMangaInfo>, fileName: String = "My_MangaDex_Follows", saveLinks: EnumSet<Links> = EnumSet.allOf(Links::class.java), bufferingMode: BufferingMode=BufferingMode.PER_TITLE) {
    val fileNameEnd: String = "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".txt"
    val homeDir: String = System.getProperty("user.home")
    var titlesFile: Path = Path(homeDir, (fileName + "_" +"Titles" + fileNameEnd))
    var linksFiles: Map<Links, Path> = saveLinks.associateBy({it}, { Path(homeDir, (fileName + "_" +it.name + fileNameEnd))})

    //stats
    var titlesAdded: Int = 0
    val nullLinks: MutableMap<Links, Int> = saveLinks.associateBy({it}, {0}).toMutableMap()

    titlesFile.createFile()
    linksFiles.forEach { it.value.createFile() }
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

    var statsFile = Path(homeDir, (fileName + "_stats" + fileNameEnd))
    statsFile.createFile()
    statsFile.writeText("Titles added: $titlesAdded\nNull links count:\n")
    for(link in saveLinks){
        statsFile.appendText("\t" + link.name + ":\t" + nullLinks[link]!!.toString())
    }
}

/**
 * Imports a list of manga to MangaUpdates by their title. Not guaranteed to work for every series, even if MangaUpdates has that series, if the title has a typo, is in a different locale than what MangaUpdates stores, or other reasons.
 * @param mangaList The list of manga to import.
 * @param muClient The [MangaUpdatesAPI.Client] to be used if [ExportOptions.MANGAUPDATES] is set.
 * @param publish A function to consume logging information. Used for [MangadexApiClientWorker.publish].
 */
suspend fun importToMangaUpdatesByTitle(muClient: MangaUpdatesAPI.Client, mangaList: MutableList<SimplifiedMangaInfo>, publish: ((Pair<String, LogType>)->Unit)? = null) {
    val readingListId = muClient.getListId("MangaDex Reading List")
    val titles: List<String> = mangaList.map { mangaInfo -> mangaInfo.title.trim({it == '\''||it=='"'||it=='\n'||it=='\r'||it==' '}) }
    publish?.invoke(Pair("Beginning MangaUpdates export...\n", LogType.STANDARD))
    for(i in 0..titles.size - 1 step 100){
        val toIndex = min(i + 100, titles.size)
        val titleCount = toIndex - i
        publish?.invoke(Pair("Exporting titles $i to ${toIndex-1}\n", LogType.STANDARD))
        val response = muClient.addTitlesToListByTitle(titles.subList(i, toIndex), readingListId)
        val responseBody = response.body<BulkTitleAddResponse>()
        if(responseBody.status.startsWith("partial")) {
            if(responseBody.context!!.errors.size == titleCount)
            {
                publish?.invoke(Pair("Failed to add titles $i to ${toIndex - 1}.\n${responseBody.context.errors.joinToString("\n","\t")}", LogType.ERROR))
            } else {
                publish?.invoke(Pair("Failed to add some titles (${responseBody.context.errors.size} of $titleCount).\n${responseBody.context.errors.joinToString("\n","\t")}",LogType.WARNING))
            }
        } else {
            publish?.invoke(Pair("Added $titleCount titles successfully\n", LogType.STANDARD))
        }
        println(responseBody)

        delay(5000)
    }
}

/**
 * Imports a list of manga to MangaUpdates by their ID. Takes a long time.
 * @param mangaList The list of manga to import.
 * @param muClient The [MangaUpdatesAPI.Client] to be used if [ExportOptions.MANGAUPDATES] is set.
 * @param publish A function to consume logging information. Used for [MangadexApiClientWorker.publish].
 */
suspend fun importToMangaUpdatesByID(muClient: MangaUpdatesAPI.Client, mangaList: MutableList<SimplifiedMangaInfo>, publish: ((Pair<String, LogType>)->Unit)? = null) {
    val readingListId = muClient.getListId("MangaDex Reading List")
    val titleIds: MutableList<String> = mutableListOf()
    var estDuration = mangaList.size.toDouble() * 5.0 / 60.0
    val durationUnits: String
    if(estDuration < 1.0) {
        estDuration *= 60
        durationUnits = "second(s)"
    } else if (estDuration > 60.0){
        estDuration /= 60
        durationUnits = "hour(s)"
    } else {
        durationUnits = "minute(s)"
    }
    publish?.invoke(Pair("Getting title IDs. This will take at least $estDuration $durationUnits.\n", LogType.STANDARD))
    for(manga in mangaList){
        if(manga.links == null || !manga.links.containsKey("mu")) {
            publish?.invoke(Pair("Ignoring ${manga.title} because it doesn't have a title\n", LogType.WARNING))
            continue
        }
        publish?.invoke(Pair("Fetching ID for ${manga.title}\n", LogType.STANDARD))
        delay(5000)
        val id = muClient.getTitleId(manga.links["mu"].toString())
        if(id.isEmpty()) publish?.invoke(Pair("Could not get MangaUpdates page for ${manga.title} (${manga.links["mu"]})\n", LogType.WARNING))
        publish?.invoke(Pair("Got ID for ${manga.title}: $id\n", LogType.STANDARD))
        titleIds.add(id)
    }
    publish?.invoke(Pair("Beginning MangaUpdates export...\n", LogType.STANDARD))
    for(i in 0..titleIds.size - 1 step 100){
        val toIndex = min(i + 100, titleIds.size)
        publish?.invoke(Pair("Exporting titles $i to ${toIndex-1}\n", LogType.STANDARD))
        try {
            val response = muClient.addTitlesToListById(titleIds.subList(i, toIndex), readingListId)
            publish?.invoke(Pair("Finished with response: ${response.body<String>()}\n", LogType.STANDARD))
        } catch (e: UnexpectedMUApiResponseException) {
            publish?.invoke(Pair("Export of titles ($i -> ${toIndex-1}) failed due to a non-success code: $e\n", LogType.ERROR))
        }

        delay(5000)
    }
}

/**
 * Creates a file to import titles to MyAnimeList via https://myanimelist.net/import.php.
 * @param mangaList The list of manga to import.
 * @param publish A function to consume logging information. Used for [MangadexApiClientWorker.publish].
 */
fun createMALFile(mangaList: MutableList<SimplifiedMangaInfo>, fileName:String = "My_MangaDex_Follows", publish: ((Pair<String, LogType>)->Unit)? = null) {
    var xml: String = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                            "<myanimelist>" +
                                "<myinfo>" +
                                    "<user_export_type>2</user_export_type>" +
                                "</myinfo>"
    for(manga in mangaList){
        if(manga.links == null || !manga.links.containsKey("mal")){
            continue
        }
        xml += "<manga>" +
                    "<manga_mangadb_id>${manga.links["mal"].toString().trim{it=='"'||it.isWhitespace()}}</manga_mangadb_id>" +
                    "<update_on_import>1</update_on_import>" +
                "</manga>"
    }
    xml += "</myanimelist>"
    val homeDir: String = System.getProperty("user.home")
    val mALFile = Path(homeDir, "${fileName}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))}.xml")
    with(mALFile){
        createFile()
        writeText(xml)
    }
}