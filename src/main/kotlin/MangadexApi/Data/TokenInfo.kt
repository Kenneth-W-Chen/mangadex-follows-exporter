package MangadexApi.Data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the response received from the MangaDex API when fetching a authorization token.
 * @param accessToken The access token (bearer).
 * @param expiresIn The number of seconds left before this token expires. Expires 15 minutes after creation.
 * @param refreshExpiresIn The number of seconds left before the refresh token expires. Expires 90 days after creation.
 * @param refreshToken The refresh token to get a new access token.
 * @param tokenType The type of access token i.e., "bearer".
 * @param notBeforePolicy Doesn't do anything for MangaDex's API; the API defaults it to 0.
 * @param sessionState No idea.
 * @param scope What parts of the API this token grants access to. This can be disregarded because MangaDex doesn't allow users/clients to set the scope.
 * @param clientType The type of API client i.e., "personal".
 */
@Serializable
data class TokenInfo(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_expires_in") val refreshExpiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("not-before-policy") val notBeforePolicy: Int,
    @SerialName("session_state") val sessionState: String,
    @SerialName("scope") val scope: String,
    @SerialName("client_type") val clientType: String
)
