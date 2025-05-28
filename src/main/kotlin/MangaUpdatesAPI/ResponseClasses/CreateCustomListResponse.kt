package MangaUpdatesAPI.ResponseClasses

import kotlinx.serialization.Serializable

/**
 * Represents the response received from the MangaUpdates API endpoint for creating a new reading list: /v1/lists/.
 * See [MangaUpdatesAPI.Client.makeList].
 * @param status The status of the request.
 * @param reason A more detailed explanation for [status].
 * @param context The ID of the new list. See [CustomListInfo]
 */
@Serializable
data class CreateCustomListResponse(
    val status: String,
    val reason: String,
    val context: CustomListInfo
)

/**
 * Object containing the ID of a new list.
 * See [MangaUpdatesAPI.Client.makeList] and [CreateCustomListResponse].
 * @param id The list's ID.
 */
@Serializable
data class CustomListInfo(
    val id: String
)
