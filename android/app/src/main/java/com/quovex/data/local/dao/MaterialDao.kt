package com.quovex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quovex.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: NoteEntity): Long

    @Update
    suspend fun updateMaterial(material: NoteEntity): Int

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllMaterials(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE subject = :subject ORDER BY updatedAt DESC")
    fun getMaterialsBySubject(subject: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getMaterialById(id: Long): NoteEntity?

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteMaterialById(id: Long): Int

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getMaterialsCount(): Int

    @Query("SELECT DISTINCT subject FROM notes WHERE subject != '' ORDER BY subject ASC")
    fun getDistinctSubjects(): Flow<List<String>>

    @Query("UPDATE notes SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: String): Int
}
