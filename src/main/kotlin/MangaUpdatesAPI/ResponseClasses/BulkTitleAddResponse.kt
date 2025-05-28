package MangaUpdatesAPI.ResponseClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the response received from the MangaUpdates API endpoint for bulk adding series to a list by title: /v1/lists/{listId}/series/bulk.
 * See [MangaUpdatesAPI.Client.addTitlesToListByTitle].
 * @param status The status of the request. Will generally be "partial-success" or "success".
 * @param reason A more detailed explanation for [status].
 * @param context Further details of issues that occurred with bulk adding. Will be null if [status] is "success".
 */
@Serializable
data class BulkTitleAddResponse(
    val status: String,
    val reason: String,
    val context: BulkTitlesAddContext? = null
)

/**
 * An array of [TitleError] that occurred when bulk adding titles. Yes, there's only one property; no, this can't be consolidated or removed.
 * See [MangaUpdatesAPI.Client.addTitlesToListByTitle] and [BulkTitleAddResponse].
 * @param errors An array of [TitleError]s that occurred when bulk adding titles.
 */
@Serializable
data class BulkTitlesAddContext(
    val errors: Array<TitleError>
)

/**
 * An issue that occurred when bulk adding titles.
 * See [MangaUpdatesAPI.Client.addTitlesToListByTitle] and [BulkTitleAddResponse].
 * @param seriesName The series name, as sent in the initial API request.
 * @param error The description of why the series wasn't added to the list. E.g., the series is already on another list, or the series doesn't exist.
 */
@Serializable
data class TitleError(
    @SerialName("series_name") val seriesName: String,
    val error: String
){
    /**
     * "Series Name" - Error
     */
    override fun toString(): String {
        return "\"$seriesName\" - $error"
    }
}