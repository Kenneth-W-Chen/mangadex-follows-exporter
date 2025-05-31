package MangaUpdatesAPI.ResponseClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the response received from the MangaUpdates API endpoint for adding series to a list by ID: /v1/lists/series.
 * See [MangaUpdatesAPI.Client.addTitlesToListById].
 * @param status The status of the request. Will generally be "partial-success" or "success".
 * @param reason A more detailed explanation for [status].
 * @param context Further details of issues that occurred with bulk adding. Will be null if [status] is "success".
 */
@Serializable
data class AddTitleByIDResponse(
    val status: String,
    val reason: String,
    val context: AddTitleByIDContext?
)

/**
 * An array of [AddTitleByIDError] that occurred when bulk adding titles. Yes, there's only one property; no, this can't be consolidated or removed.
 * See [MangaUpdatesAPI.Client.addTitlesToListById] and [AddTitleByIDResponse].
 * @param errors An array of [AddTitleByIDError]s that occurred when bulk adding titles.
 */
@Serializable
data class AddTitleByIDContext(
    val errors: List<AddTitleByIDError>
)

/**
 * An issue that occurred when adding series by their ID.
 * See [MangaUpdatesAPI.Client.addTitlesToListById] and [AddTitleByIDContext].
 * @param seriesId The series' MangaUpdates ID (or the ID that was given to MangaUpdates).
 * @param error The description of why the series wasn't added to the list. E.g., the series is already on another list, or the series doesn't exist.
 */
@Serializable
data class AddTitleByIDError(
    @SerialName("series_id") val seriesId: String,
    val error: String
){
    /**
     * "Series Name" - Error
     */
    override fun toString(): String {
        return "ID: $seriesId - $error"
    }
}