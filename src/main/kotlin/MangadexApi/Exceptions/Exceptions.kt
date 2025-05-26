package MangadexApi.Exceptions

class InvalidMDUserCredentialsException: Exception("Invalid user or client credentials.")
class UnexpectedMDApiResponseException(message: String): Exception(message)
