package MangadexApi.Data

import com.sun.tools.javac.api.Formattable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class MangaInfoResponse(
    val result: String,
    val response: String,
    val data: Array<MangaInfo>,
    val limit: Int,
    val offset: Int,
    val total: Int,
)

@Serializable
data class MangaInfo(
    val id: String,
    val type: String,
    val attributes: MangaAttributes,
    val relationships: Array<JsonObject>
)
@Serializable
data class MangaAttributes(
    val title: JsonObject,
    val altTitles: Array<JsonObject>,
    val description: JsonObject,
    val isLocked: Boolean,
    val links: JsonObject,
    val originalLanguage: String,
    val lastVolume: String?,
    val lastChapter: String?,
    val publicationDemographic: String?,
    val status: String,
    val year: Int?,
    val contentRating: String?,
    val chapterNumbersResetOnNewVolume: Boolean,
    val availableTranslatedLanguages: Array<String>,
    val latestUploadedChapter: String,
    val tags: Array<JsonObject>,
    val state:String,
    val version: Int,
    val createdAt: String,
    val updatedAt: String,
)
