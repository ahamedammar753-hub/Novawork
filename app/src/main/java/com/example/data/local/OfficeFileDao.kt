package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OfficeFile
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficeFileDao {
    @Query("SELECT * FROM office_files WHERE isInTrash = 0 ORDER BY lastModified DESC")
    fun getActiveFiles(): Flow<List<OfficeFile>>

    @Query("SELECT * FROM office_files WHERE isInTrash = 0 ORDER BY lastModified DESC LIMIT :limit")
    fun getRecentFiles(limit: Int = 10): Flow<List<OfficeFile>>

    @Query("SELECT * FROM office_files WHERE isInTrash = 0 AND isFavorite = 1 ORDER BY lastModified DESC")
    fun getFavoriteFiles(): Flow<List<OfficeFile>>

    @Query("SELECT * FROM office_files WHERE isInTrash = 1 ORDER BY lastModified DESC")
    fun getTrashFiles(): Flow<List<OfficeFile>>

    @Query("SELECT * FROM office_files WHERE isInTrash = 0 AND fileType = :type ORDER BY lastModified DESC")
    fun getFilesByType(type: String): Flow<List<OfficeFile>>

    @Query("SELECT * FROM office_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): OfficeFile?

    @Query("SELECT * FROM office_files WHERE id = :id LIMIT 1")
    fun getFileByIdFlow(id: Long): Flow<OfficeFile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: OfficeFile): Long

    @Update
    suspend fun updateFile(file: OfficeFile)

    @Query("UPDATE office_files SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE office_files SET isInTrash = 1 WHERE id = :id")
    suspend fun moveToTrash(id: Long)

    @Query("UPDATE office_files SET isInTrash = 0 WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("DELETE FROM office_files WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM office_files WHERE isInTrash = 1")
    suspend fun emptyTrash()

    @Query("SELECT * FROM office_files WHERE isInTrash = 0 AND (title LIKE '%' || :query || '%' OR contentJson LIKE '%' || :query || '%') ORDER BY lastModified DESC")
    fun searchFiles(query: String): Flow<List<OfficeFile>>
}
