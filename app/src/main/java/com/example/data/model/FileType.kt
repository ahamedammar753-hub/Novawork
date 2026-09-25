package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.NovaCobalt
import com.example.ui.theme.NovaCrimson
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaOrange
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaTeal

enum class FileType(
    val displayName: String,
    val extension: String,
    val icon: ImageVector,
    val color: Color
) {
    DOCUMENT("Document", ".docx", Icons.Default.Description, NovaCobalt),
    SPREADSHEET("Spreadsheet", ".xlsx", Icons.Default.GridOn, NovaEmerald),
    PRESENTATION("Presentation", ".pptx", Icons.Default.Slideshow, NovaOrange),
    PUBLISHER("Publisher", ".pub", Icons.Default.ViewQuilt, NovaPurple),
    FORM("Form", ".form", Icons.Default.Feed, NovaTeal),
    PDF("PDF Document", ".pdf", Icons.Default.PictureAsPdf, NovaCrimson);

    companion object {
        fun fromString(type: String): FileType {
            return entries.firstOrNull { it.name.equals(type, ignoreCase = true) } ?: DOCUMENT
        }
    }
}
