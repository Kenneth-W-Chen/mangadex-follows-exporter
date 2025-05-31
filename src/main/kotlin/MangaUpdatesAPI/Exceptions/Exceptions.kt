package MangaUpdatesAPI.Exceptions

import io.ktor.client.statement.HttpResponse

/**
 * Thrown when the MangaUpdates API rejects the provided user credentials.
 */
class InvalidMUCredentialsException : Exception("Invalid MangaUpdates user credentials.")

/**
 * Thrown when the API client receives an unexpected response code from the server.
 */
class UnexpectedMUApiResponseException(message: String, val response: HttpResponse): Exception(message)