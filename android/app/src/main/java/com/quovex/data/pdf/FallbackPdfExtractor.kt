package com.quovex.data.pdf

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FALLBACK-ONLY offline PDF metadata / text reader helper.
 * Primary production pipeline executes server-side via Cloud Function + Storage upload.
 */
@Singleton
class FallbackPdfExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun readBasicPdfHeader(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val buffer = ByteArray(1024)
            val bytesRead = inputStream?.use { it.read(buffer) } ?: 0
            if (bytesRead > 0 && String(buffer, 0, bytesRead).contains("%PDF-")) {
                Result.success("Valid PDF Document")
            } else {
                Result.failure(IllegalArgumentException("Selected file is not a valid PDF"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
