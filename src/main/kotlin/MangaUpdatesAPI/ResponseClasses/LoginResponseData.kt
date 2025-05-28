package MangaUpdatesAPI.ResponseClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the response received from the MangaUpdates API endpoint for bulk adding series to a list by title: /v1/account/login
 * See [MangaUpdatesAPI.Client.fetchToken].
 * @param status The status of the request. Will generally be "success" or "exception".
 * @param reason A more detailed explanation for [status].
 * @param context The login token.
 */
@Serializable
data class LoginResponseData (
    val status: String,
    val reason: String,
    val context: LoginToken?
)

/**
 * Login information for MangaUpdates.
 * See [MangaUpdatesAPI.Client.fetchToken] and [LoginResponseData]
 * @param sessionToken The session token. This doesn't expire for a long time (at least a week).
 * @param uid The user ID associated with [sessionToken]
 */
@Serializable
data class LoginToken (
    @SerialName("session_token") val sessionToken: String,
    val uid: String
)