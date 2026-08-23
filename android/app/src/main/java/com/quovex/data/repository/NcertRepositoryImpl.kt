package com.quovex.data.repository

import android.content.Context
import com.google.gson.Gson
import com.quovex.data.remote.AiGatewayApiService
import com.quovex.data.remote.dto.NcertCatalogResponseDto
import com.quovex.data.remote.dto.toDomain
import com.quovex.domain.model.NcertBook
import com.quovex.domain.model.NcertCatalog
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.repository.NcertRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NcertRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: AiGatewayApiService
) : NcertRepository {

    private val gson: Gson = Gson()
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _catalog = MutableStateFlow(NcertCatalog())

    init {
        // 1. Immediately bootstrap from bundled local catalog
        loadBundledCatalog()

        // 2. Asynchronously check remote catalog
        CoroutineScope(ioDispatcher).launch {
            refreshCatalog()
        }
    }

    override fun getCatalog(): Flow<NcertCatalog> = _catalog.asStateFlow()

    override fun getAvailableClasses(): Flow<List<Int>> {
        return _catalog.map { cat ->
            cat.books.map { it.classLevel }.distinct().sorted()
        }
    }

    override fun getSubjectsForClass(classLevel: Int): Flow<List<String>> {
        return _catalog.map { cat ->
            cat.books
                .filter { it.classLevel == classLevel }
                .map { it.subject }
                .distinct()
                .sorted()
        }
    }

    override fun getBooks(classLevel: Int?, subject: String?): Flow<List<NcertBook>> {
        return _catalog.map { cat ->
            cat.books.filter { book ->
                (classLevel == null || book.classLevel == classLevel) &&
                (subject.isNullOrBlank() || subject.equals("All", ignoreCase = true) || book.subject.equals(subject, ignoreCase = true))
            }
        }
    }

    override fun getBookById(bookId: String): Flow<NcertBook?> {
        return _catalog.map { cat ->
            cat.books.find { it.id == bookId }
        }
    }

    override fun getChaptersForBook(bookId: String): Flow<List<NcertChapter>> {
        return _catalog.map { cat ->
            cat.chapters
                .filter { it.bookId == bookId }
                .sortedBy { it.chapterNumber }
        }
    }

    override fun getChapterById(chapterId: String): Flow<NcertChapter?> {
        return _catalog.map { cat ->
            cat.chapters.find { it.id == chapterId }
        }
    }

    override suspend fun refreshCatalog(): Result<NcertCatalog> = withContext(ioDispatcher) {
        try {
            val response = apiService.getNcertCatalog()
            if (response.isSuccessful && response.body() != null) {
                val remoteCatalog = response.body()!!.toDomain()
                if (remoteCatalog.books.isNotEmpty()) {
                    _catalog.value = remoteCatalog
                    return@withContext Result.success(remoteCatalog)
                }
            }
            // If remote is empty or unsuccessful, keep local catalog
            Result.success(_catalog.value)
        } catch (e: Exception) {
            // Remote unavailable / offline -> gracefully fall back to local catalog
            Result.success(_catalog.value)
        }
    }

    fun loadBundledCatalog(): Boolean {
        return try {
            val jsonString = context.assets.open("ncert/ncert_catalog_v1.json")
                .bufferedReader()
                .use { it.readText() }
            val dto = gson.fromJson(jsonString, NcertCatalogResponseDto::class.java)
            if (dto != null) {
                _catalog.value = dto.toDomain()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Testing / Mocking helper
     */
    fun setCatalogForTesting(catalog: NcertCatalog) {
        _catalog.value = catalog
    }
}


