package com.example.ui.navigation

sealed class Screen {
    object Home : Screen()
    data class DocumentEditor(val fileId: Long) : Screen()
    data class SpreadsheetEditor(val fileId: Long) : Screen()
    data class PresentationEditor(val fileId: Long) : Screen()
    data class PublisherEditor(val fileId: Long) : Screen()
    data class PdfTools(val fileId: Long? = null) : Screen()
    data class FormEditor(val fileId: Long) : Screen()
    data class FileManager(val initialCategory: String = "ALL") : Screen()
    data class OfficeTools(val selectedTool: String = "MENU") : Screen()
    object Scanner : Screen()
    object Drawing : Screen()
    object Settings : Screen()
}
