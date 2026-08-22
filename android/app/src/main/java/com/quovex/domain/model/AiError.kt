package com.quovex.domain.model

sealed class AiError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class AuthenticationError(message: String = "Firebase user authentication required.", cause: Throwable? = null) : AiError(message, cause)
    class RateLimitError(message: String = "AI rate limit reached. Please wait a moment.", cause: Throwable? = null) : AiError(message, cause)
    class NetworkError(message: String = "Network connection failed. Check your internet.", cause: Throwable? = null) : AiError(message, cause)
    class TimeoutError(message: String = "AI request timed out. Please try again.", cause: Throwable? = null) : AiError(message, cause)
    class ProviderUnavailableError(message: String = "AI services are currently busy or unavailable.", cause: Throwable? = null) : AiError(message, cause)
    class InvalidRequestError(message: String = "Invalid request payload sent to AI gateway.", cause: Throwable? = null) : AiError(message, cause)
    class InvalidResponseError(message: String = "Received invalid response from AI gateway.", cause: Throwable? = null) : AiError(message, cause)
    class UnknownAIError(message: String = "An unexpected AI error occurred.", cause: Throwable? = null) : AiError(message, cause)
}
