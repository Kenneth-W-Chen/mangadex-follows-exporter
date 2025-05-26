package MangaUpdatesAPI.Exceptions

class InvalidMUCredentialsException : Exception("Invalid MangaUpdates user credentials.")
class UnexpectedMUApiResponseException(message: String): Exception(message)