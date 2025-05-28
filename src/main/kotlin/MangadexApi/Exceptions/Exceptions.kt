package MangadexApi.Exceptions

/**
 * Thrown when the MangaDex API rejects the provided user's log-in credentials or APi credentials.
 */
class InvalidMDUserCredentialsException: Exception("Invalid user or client credentials.")

/**
 * Thrown when the API client receives an unexpected response code from the server.
 */
class UnexpectedMDApiResponseException(message: String): Exception(message)
