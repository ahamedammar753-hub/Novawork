package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "office_files")
data class OfficeFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val fileType: String, // from FileType.name
    val contentJson: String,
    val lastModified: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 1024L,
    val isFavorite: Boolean = false,
    val isInTrash: Boolean = false,
    val category: String = "General"
) {
    fun getTypedType(): FileType = FileType.fromString(fileType)

    fun formattedDate(): String {
        val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
        return sdf.format(Date(lastModified))
    }

    fun formattedSize(): String {
        if (sizeBytes < 1024) return "$sizeBytes B"
        val kb = sizeBytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format(Locale.US, "%.2f MB", mb)
    }
}
