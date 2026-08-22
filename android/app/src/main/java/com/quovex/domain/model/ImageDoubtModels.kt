package com.quovex.domain.model

/**
 * Domain-safe image input abstraction.
 * Encapsulates raw image binary data without leaking Android-specific
 * Bitmap or Context objects into the domain layer.
 */
data class DomainImageInput(
    val bytes: ByteArray,
    val mimeType: String = "image/jpeg"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DomainImageInput

        if (!bytes.contentEquals(other.bytes)) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Domain model representing the step-by-step solution from the vision AI tutor.
 */
data class ImageDoubtSolution(
    val solution: String,
    val provider: String = "groq",
    val model: String? = null
)
