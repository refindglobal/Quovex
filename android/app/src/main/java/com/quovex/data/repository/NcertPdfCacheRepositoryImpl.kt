package com.quovex.data.repository

import android.content.Context
import com.quovex.domain.repository.NcertPdfCacheRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Implementation of [NcertPdfCacheRepository].
 *
 * Downloads NCERT PDFs from official source URLs to the app's temporary cache
 * (cacheDir/ncert_pdfs/). Uses OkHttp with legacy TLS fallback and automatic
 * HTTP-to-HTTPS redirect resolution.
 */
@Singleton
class NcertPdfCacheRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NcertPdfCacheRepository {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val okHttpClient: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    private fun pdfCacheDir(): java.io.File {
        val dir = java.io.File(context.cacheDir, "ncert_pdfs")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    override fun getCachedPdfPath(chapterId: String): String? {
        val file = java.io.File(pdfCacheDir(), "${chapterId}.pdf")
        return if (file.exists() && file.length() > 0) file.absolutePath else null
    }

    override suspend fun downloadPdfToCache(chapterId: String, url: String): Result<String> =
        withContext(ioDispatcher) {
            try {
                // Return cached path if already downloaded
                getCachedPdfPath(chapterId)?.let { return@withContext Result.success(it) }

                val (httpsUrl, httpUrl) = resolveNcertDirectPdfUrls(url)
                val destFile = java.io.File(pdfCacheDir(), "${chapterId}.pdf")
                val tempFile = java.io.File(pdfCacheDir(), "${chapterId}.tmp")

                val encodedHttps = java.net.URLEncoder.encode(httpsUrl, "UTF-8")
                val urlsToTry = mutableListOf(
                    httpsUrl,
                    httpUrl
                )
                if (com.quovex.BuildConfig.DEBUG) {
                    urlsToTry.add("http://10.0.2.2:5001/ncert/pdf?url=$encodedHttps")
                    urlsToTry.add("http://127.0.0.1:5001/ncert/pdf?url=$encodedHttps")
                }
                urlsToTry.add("https://api-dopkbhqrgq-uc.a.run.app/ncert/pdf?url=$encodedHttps")

                var downloadSuccess = false
                var lastError: Exception? = null

                for (targetUrl in urlsToTry) {
                    try {
                        if (com.quovex.BuildConfig.DEBUG) {
                            android.util.Log.d("NcertPdfCache", "Attempting download chapterId=$chapterId from URL: $targetUrl")
                        }
                        val request = Request.Builder()
                            .url(targetUrl)
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .header("Accept", "*/*")
                            .build()

                        okHttpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val body = response.body
                                if (body != null) {
                                    tempFile.outputStream().use { out ->
                                        body.byteStream().copyTo(out)
                                    }
                                    if (tempFile.length() > 1024L) {
                                        downloadSuccess = true
                                    } else {
                                        tempFile.delete()
                                    }
                                }
                            }
                        }

                        if (downloadSuccess) break
                    } catch (e: Exception) {
                        lastError = e
                    }
                }

                if (!downloadSuccess || !tempFile.exists() || tempFile.length() == 0L) {
                    tempFile.delete()
                    return@withContext Result.failure(
                        Exception("NCERT content is temporarily unavailable. ${lastError?.message ?: ""}")
                    )
                }

                val success = tempFile.renameTo(destFile)
                if (!success) {
                    tempFile.copyTo(destFile, overwrite = true)
                    tempFile.delete()
                }

                Result.success(destFile.absolutePath)
            } catch (e: Exception) {
                android.util.Log.e("NcertPdfCache", "Download failed for chapterId=$chapterId", e)
                Result.failure(Exception("NCERT content is temporarily unavailable. ${e.message}"))
            }
        }

    override suspend fun clearPdfCache(): Result<Unit> = withContext(ioDispatcher) {
        try {
            pdfCacheDir().listFiles()?.forEach { it.delete() }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resolveNcertDirectPdfUrls(url: String): Pair<String, String> {
        val regex = Regex("""https?://(?:www\.)?ncert\.nic\.in/textbook\.php\?([a-zA-Z0-9]+)=(\d+)-(\d+)""")
        val match = regex.find(url)
        return if (match != null) {
            val (bookCode, chapterStr, _) = match.destructured
            val chapterNum = chapterStr.toIntOrNull() ?: 1
            val paddedChapter = "%02d".format(chapterNum)
            Pair(
                "https://ncert.nic.in/textbook/pdf/${bookCode}${paddedChapter}.pdf",
                "http://ncert.nic.in/textbook/pdf/${bookCode}${paddedChapter}.pdf"
            )
        } else {
            val https = if (url.startsWith("http://")) url.replace("http://", "https://") else url
            val http = if (url.startsWith("https://")) url.replace("https://", "http://") else url
            Pair(https, http)
        }
    }
}

