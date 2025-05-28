package MangaUpdatesAPI.ResponseClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents the full information of a reading list on MangaUpdates.
 * See [MangaUpdatesAPI.Client.fetchLists].
 * @param title The title of the list.
 * @param description The description of the list.
 * @param type The type of list. See [ListType].
 * @param icon The HTML element for the list's icon.
 * @param custom Whether the list is a custom list or one of the default lists provided by MangaUpdates (i.e., Reading List, Wish List, Complete List, Unfinished List, On Hold List).
 * @param options Specific options for the list. Despite the API documentation saying otherwise, this doesn't get returned.
 */
@Serializable
data class ListData(
    @SerialName("list_id") val listId: String,
    val title: String,
    val description: String,
    val type: ListType,
    val icon: String,
    val custom: Boolean,
    val options: JsonObject? = null
)

/**
 * The type of MangaUpdates list.
 */
@Serializable
enum class ListType{
    @SerialName("read") READ,
    @SerialName("wish") WISH,
    @SerialName("complete") COMPLETE,
    @SerialName("unfinished") UNFINISHED,
    @SerialName("hold") HOLD,
}