package MangadexApi.Data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.EnumSet
import kotlin.collections.forEach

/**
 * Represents the response from the following MangaDex API endpoint: user/follows/manga.
 * @property result Server response details. If a HTTP200 was returned, this will probably be "ok".
 * @property response Response data type. This will be "collection".
 * @property data An array of [MangaInfo].
 * @property limit The max number of titles the API was told to send back in the initial API call. The API will return a number of titles up to limit, depending on server load and the number of titles that can be fetched.
 * @property offset The initial index the fetch was started with e.g., an offset of 30 with a limit of 20 would fetch titles 30 - 49 (inclusive) for a total of 20 titles.
 * @property total The total number of titles contained in this response.
 */
@Serializable
data class MangaInfoResponse(
    val result: String,
    val response: String,
    val data: Array<MangaInfo>,
    val limit: Int,
    val offset: Int,
    val total: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MangaInfoResponse

        if (limit != other.limit) return false
        if (offset != other.offset) return false
        if (total != other.total) return false
        if (result != other.result) return false
        if (response != other.response) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = limit
        result1 = 31 * result1 + offset
        result1 = 31 * result1 + total
        result1 = 31 * result1 + result.hashCode()
        result1 = 31 * result1 + response.hashCode()
        result1 = 31 * result1 + data.contentHashCode()
        return result1
    }
}

/**
 * Represents a single manga's info, as returned from the following MangaDex API endpoint: user/follows/manga.
 * @property id MangaDex series ID. Can be used to get the series page via https://www.mangadex.org/title/{id}.
 * @property type Should be "manga". Describes what the object type is.
 * @property attributes Information related to the manga. See [MangaAttributes] for more details.
 * @property relationships Related items to the series, including author, artist, and cover art.
 */
@Serializable
data class MangaInfo(
    val id: String,
    val type: String,
    val attributes: MangaAttributes,
    val relationships: Array<JsonObject>
) {
    /**
     * Converts this to a [SimplifiedMangaInfo] object.
     * @param titleLocalePreference Sets the priority for title selection for [SimplifiedMangaInfo.title]. Use the ISO-639 standard for language codes. Append "-ro" if you want a romanized version (if applicable to the language). Default is Japanese, Japanese (romanized), Korean, Korean (romanized), Chinese, Chinese - Hong Kong, Chinese (romanized), and English.
     */
    fun toSimplifiedMangaInfo(titleLocalePreference: Array<String> = arrayOf("ja", "ja-ro", "ko", "ko-ro", "zh", "zh-hk", "zh-ro", "en")): SimplifiedMangaInfo {
        var title: String = attributes.title.values.first().toString()

        /**
         * All this stupid logic is to deal with this stupid notation:
         *
         * altTitles:{
         *  {
         *      "ja-ro": "Taitoru"
         *  },
         *  {
         *      "ja-ro": "Taitoru demo nagai"
         *  },
         *  {
         *      "en": "Title"
         *  }
         * }
         *
         *
         * Instead of
         *
         * altTitles:{
         *  "ja-ro": ["Taitoru", "Taitoru demo nagai"],
         *  "en": ["Title"]
         * }
         *
         *
         *
         * or similar. This is how simple it would look if it was formatted better:
         *      for(locale in languageCodePriorities){
         *          title = attributes.title.get(locale)?:attributes.altTitles.get(locale)[0] // or something similar, not writing proper logic for this
         *          if(title != null) break
         *      }
         *
         * And I'm not writing my own reserializing logic for this garbage.
         */
        for (locale in titleLocalePreference) {
            val localePreference: String
            if(locale.lowercase() == "original") {
                localePreference = attributes.originalLanguage.toString()
                if(localePreference == "null") continue
            }
            else
                localePreference = locale.lowercase()

            if (attributes.title[localePreference].toString() != "null") {
                title = attributes.title[localePreference].toString()
                break
            }
            if (attributes.altTitles.isNotEmpty()) {
                val altTitle =
                    attributes.altTitles.firstNotNullOfOrNull { t -> t.values.takeIf { t.containsKey(localePreference) } }
                if (altTitle != null) {
                    title = altTitle.first().toString()
                    break
                }
            }
        }
        return SimplifiedMangaInfo(title, attributes.links)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MangaInfo

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + attributes.hashCode()
        result = 31 * result + relationships.contentHashCode()
        return result
    }
}

// Mangadex documentation doesn't properly list nullable fields :)
/**
 * Represents the attributes for a manga title. Since API documentation doesn't mention what fields are nullable, every field is nullable for this class.
 * @param title A [JsonObject] where the property name is the display language? and value is the title for that display language?. JP-RO may show up for a "en" display.
 * @param altTitles An array of objects whose single property has the following attributes: property name is the locale; value is the title in that locale.
 * @param description The manga's description. Property keys are a locale, and values are the description in that locale. Some locales will not be present.
 * @param isLocked Represents whether the title is locked or not. No documentation provided.
 * @param links The external links or IDs for the manga. See: https://api.mangadex.org/docs/3-enumerations/#manga-links-data
 * @param originalLanguage A string representing the original locale language.
 * @param lastVolume The latest volume number for the manga.
 * @param lastChapter The latest chapter number for the manga.
 * @param publicationDemographic The publication demographic e.g., shounen or seinen.
 * @param status The publication status i.e., completed, ongoing, cancelled.
 * @param year The initial publication year.
 * @param contentRating The manga content rating e.g., safe or explicit
 * @param chapterNumbersResetOnNewVolume Represents whether the chapter numbers increment between volumes (e.g., if volume 1 ends on chapter 5, volume 2 may start at chapter 6 or chapter 1).
 * @param availableTranslatedLanguages An array of language locales that MangaDex has chapters in.
 * @param latestUploadedChapter Manga Chapter ID of the latest chapter
 * @param tags An array of genre tags for the manga, set by MangaDex and uploaders.
 * @param state No idea what this is. Of 100 titles, all were "published". Might represent where the series was published e.g., self-published (twitter [currently x], pixiv, etc.) or published (official publisher).
 * @param version No idea.
 * @param createdAt When the series was added to MangaDex.
 * @param updatedAt The last time the series was updated on MangaDex.
 */
@Serializable
data class MangaAttributes(
    val title: JsonObject,
    val altTitles: Array<JsonObject>,
    val description: JsonObject?,
    val isLocked: Boolean?,
    val links: JsonObject?,
    val originalLanguage: String?,
    val lastVolume: String?,
    val lastChapter: String?,
    val publicationDemographic: String?,
    val status: String?,
    val year: Int?,
    val contentRating: String?,
    val chapterNumbersResetOnNewVolume: Boolean?,
    val availableTranslatedLanguages: Array<String>?,
    val latestUploadedChapter: String?,
    val tags: Array<JsonObject>?,
    val state: String?,
    val version: Int?,
    val createdAt: String?,
    val updatedAt: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MangaAttributes

        if (isLocked != other.isLocked) return false
        if (year != other.year) return false
        if (chapterNumbersResetOnNewVolume != other.chapterNumbersResetOnNewVolume) return false
        if (version != other.version) return false
        if (title != other.title) return false
        if (!altTitles.contentEquals(other.altTitles)) return false
        if (description != other.description) return false
        if (links != other.links) return false
        if (originalLanguage != other.originalLanguage) return false
        if (lastVolume != other.lastVolume) return false
        if (lastChapter != other.lastChapter) return false
        if (publicationDemographic != other.publicationDemographic) return false
        if (status != other.status) return false
        if (contentRating != other.contentRating) return false
        if (!availableTranslatedLanguages.contentEquals(other.availableTranslatedLanguages)) return false
        if (latestUploadedChapter != other.latestUploadedChapter) return false
        if (!tags.contentEquals(other.tags)) return false
        if (state != other.state) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLocked?.hashCode() ?: 0
        result = 31 * result + (year ?: 0)
        result = 31 * result + (chapterNumbersResetOnNewVolume?.hashCode() ?: 0)
        result = 31 * result + (version ?: 0)
        result = 31 * result + title.hashCode()
        result = 31 * result + altTitles.contentHashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (links?.hashCode() ?: 0)
        result = 31 * result + (originalLanguage?.hashCode() ?: 0)
        result = 31 * result + (lastVolume?.hashCode() ?: 0)
        result = 31 * result + (lastChapter?.hashCode() ?: 0)
        result = 31 * result + (publicationDemographic?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (contentRating?.hashCode() ?: 0)
        result = 31 * result + (availableTranslatedLanguages?.contentHashCode() ?: 0)
        result = 31 * result + (latestUploadedChapter?.hashCode() ?: 0)
        result = 31 * result + (tags?.contentHashCode() ?: 0)
        result = 31 * result + (state?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }
}

/**
 * Represents a manga with its title and associated links.
 * @param title The manga title.
 * @param links The external links or IDs for the manga. See: https://api.mangadex.org/docs/3-enumerations/#manga-links-data
 */
@Serializable
data class SimplifiedMangaInfo(
    val title: String,
    val links: JsonObject?,
)
