package MangadexApi.Exceptions

class InvalidUserCredentialsException: Exception("Invalid user or client credentials.")
class UnexpectedResponseException(message: String): Exception(message)