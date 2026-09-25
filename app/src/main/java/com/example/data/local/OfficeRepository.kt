package com.example.data.local

import com.example.data.model.FileType
import com.example.data.model.OfficeFile
import kotlinx.coroutines.flow.Flow

class OfficeRepository(private val dao: OfficeFileDao) {

    val activeFiles: Flow<List<OfficeFile>> = dao.getActiveFiles()
    val recentFiles: Flow<List<OfficeFile>> = dao.getRecentFiles(8)
    val favoriteFiles: Flow<List<OfficeFile>> = dao.getFavoriteFiles()
    val trashFiles: Flow<List<OfficeFile>> = dao.getTrashFiles()

    fun getFilesByType(type: String): Flow<List<OfficeFile>> = dao.getFilesByType(type)

    fun getFileByIdFlow(id: Long): Flow<OfficeFile?> = dao.getFileByIdFlow(id)

    suspend fun getFileById(id: Long): OfficeFile? = dao.getFileById(id)

    suspend fun createFile(title: String, type: FileType, content: String? = null, category: String = "General"): Long {
        val initialContent = content ?: when (type) {
            FileType.DOCUMENT -> OfficeTemplates.SAMPLE_DOCUMENT_CONTENT
            FileType.SPREADSHEET -> OfficeTemplates.SAMPLE_SPREADSHEET_CONTENT
            FileType.PRESENTATION -> OfficeTemplates.SAMPLE_PRESENTATION_CONTENT
            FileType.PUBLISHER -> OfficeTemplates.SAMPLE_PUBLISHER_CONTENT
            FileType.FORM -> OfficeTemplates.SAMPLE_FORM_CONTENT
            FileType.PDF -> OfficeTemplates.SAMPLE_PDF_CONTENT
        }

        val newFile = OfficeFile(
            title = title.ifBlank { "Untitled ${type.displayName}" },
            fileType = type.name,
            contentJson = initialContent,
            lastModified = System.currentTimeMillis(),
            sizeBytes = initialContent.toByteArray().size.toLong().coerceAtLeast(1024L),
            category = category
        )
        return dao.insertFile(newFile)
    }

    suspend fun saveFile(file: OfficeFile) {
        val updated = file.copy(
            lastModified = System.currentTimeMillis(),
            sizeBytes = file.contentJson.toByteArray().size.toLong().coerceAtLeast(1024L)
        )
        dao.updateFile(updated)
    }

    suspend fun updateFileTitle(id: Long, newTitle: String) {
        val existing = dao.getFileById(id) ?: return
        dao.updateFile(existing.copy(title = newTitle, lastModified = System.currentTimeMillis()))
    }

    suspend fun toggleFavorite(id: Long, currentIsFavorite: Boolean) {
        dao.setFavorite(id, !currentIsFavorite)
    }

    suspend fun moveToTrash(id: Long) {
        dao.moveToTrash(id)
    }

    suspend fun restoreFromTrash(id: Long) {
        dao.restoreFromTrash(id)
    }

    suspend fun deletePermanently(id: Long) {
        dao.deletePermanently(id)
    }

    suspend fun emptyTrash() {
        dao.emptyTrash()
    }

    suspend fun duplicateFile(file: OfficeFile): Long {
        val copy = file.copy(
            id = 0,
            title = "${file.title} (Copy)",
            lastModified = System.currentTimeMillis(),
            isFavorite = false,
            isInTrash = false
        )
        return dao.insertFile(copy)
    }

    fun searchFiles(query: String): Flow<List<OfficeFile>> = dao.searchFiles(query)
}
