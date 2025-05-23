package Utilities

import MangadexApi.Data.SimplifiedMangaInfo
import java.nio.file.Path
import kotlin.io.path.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.EnumSet
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.io.path.*

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



enum class BufferingMode{
    PER_TITLE,
    PER_LIST
}

enum class ExportOptions{
    TXT,
    CSV,
    MANGAUPDATES
}

fun exportMangaList(mangaList:MutableList<SimplifiedMangaInfo>, fileName: String = "My_MangaDex_Follows", saveLinks: EnumSet<Links> = EnumSet.allOf(Links::class.java), exportOptions: EnumSet<ExportOptions> = EnumSet.allOf(
    ExportOptions::class.java), bufferingMode: BufferingMode=BufferingMode.PER_TITLE){
    val homeDir: String = System.getProperty("user.home")
    var titlesFile: Path? = null
    var linksFiles: Map<Links, Path>? = null
    var csvFile: Path? = null
    val makeTxt = exportOptions.contains(ExportOptions.TXT)
    val makeCsv = exportOptions.contains(ExportOptions.CSV)
    val exportMangaUpdates = exportOptions.contains(ExportOptions.MANGAUPDATES)
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
    fun mangaUpdatesExport(){
        //todo
    }
    when(bufferingMode){
        BufferingMode.PER_TITLE -> {
            if(makeCsv) csvFile!!.appendText("title,${saveLinks.joinToString(",", transform = { it.name })}\n")
            for(manga in mangaList){
                if(makeTxt) titlesFile!!.appendText(manga.title + "\n")
                var csvLine = ""
                if(makeCsv) csvLine += "${manga.title},"
                if(exportMangaUpdates) mangaUpdatesExport()
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
                if(exportMangaUpdates) mangaUpdatesExport()
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
}

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