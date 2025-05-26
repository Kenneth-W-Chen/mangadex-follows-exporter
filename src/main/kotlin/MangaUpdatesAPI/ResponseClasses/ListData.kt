package MangaUpdatesAPI.ResponseClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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

@Serializable
enum class ListType{
    @SerialName("read") READ,
    @SerialName("wish") WISH,
    @SerialName("complete") COMPLETE,
    @SerialName("unfinished") UNFINISHED,
    @SerialName("hold") HOLD,
}