package MangaUpdatesAPI.ResponseClasses

import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomListResponse(
    val status: String,
    val reason: String,
    val context: CustomListInfo
)

@Serializable
data class CustomListInfo(
    val id: String
)
