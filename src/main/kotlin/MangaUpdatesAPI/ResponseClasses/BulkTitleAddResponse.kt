package MangaUpdatesAPI.ResponseClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BulkTitleAddResponse(
    val status: String,
    val reason: String,
    val context: BulkTitlesAddContext? = null
)

@Serializable
data class BulkTitlesAddContext(
    val errors: Array<TitleError>
)

@Serializable
data class TitleError(
    @SerialName("series_name") val seriesName: String,
    val error: String
){
    override fun toString(): String {
        return "\"$seriesName\" - $error"
    }
}