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
) {
    fun toSimplifiedMangaInfo(preferRomaji: Boolean = false): SimplifiedMangaInfo {
        var title: String = attributes.title.values.first().toString()
        val languageCodesRomaji: Array<String> = arrayOf("ja-ro", "ko-ro", "zh-ro")
        val languageCodes: Array<String> = arrayOf("ja", "ko", "zh", "zh-hk")
        var languageCodePriorities: Array<String>
        if (preferRomaji) {
            languageCodePriorities = languageCodesRomaji.plus(languageCodes).plus("en")
        } else {
            languageCodePriorities = languageCodes.plus(languageCodesRomaji).plus("en")
        }

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
        for (locale in languageCodePriorities) {
            if (attributes.title[locale].toString() != "null") {
                title = attributes.title[locale].toString()
                break
            }
            if (attributes.altTitles.isNotEmpty()) {
                val altTitle =
                    attributes.altTitles.firstNotNullOfOrNull { t -> t.values.takeIf { t.containsKey(locale) } }
                if (altTitle != null) {
                    title = altTitle.first().toString()
                    break
                }
            }
        }
        return SimplifiedMangaInfo(title, attributes.links)
    }
}

// Mangadex documentation doesn't properly list nullable fields :)
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
)

@Serializable
data class SimplifiedMangaInfo(
    val title: String,
    val links: JsonObject?,
)
