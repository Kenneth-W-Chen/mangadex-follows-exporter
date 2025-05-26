package MangaUpdatesAPI.ResponseClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseData (
    val status: String,
    val reason: String,
    val context: LoginCredentialsData?
)

@Serializable
data class LoginCredentialsData (
    @SerialName("session_token") val sessionToken: String,
    val uid: String
)