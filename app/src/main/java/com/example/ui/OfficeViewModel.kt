package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.OfficeRepository
import com.example.data.model.FileType
import com.example.data.model.OfficeFile
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OfficeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OfficeRepository

    val activeFiles: StateFlow<List<OfficeFile>>
    val recentFiles: StateFlow<List<OfficeFile>>

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeEditingFile = MutableStateFlow<OfficeFile?>(null)
    val activeEditingFile: StateFlow<OfficeFile?> = _activeEditingFile.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).officeFileDao()
        repository = OfficeRepository(dao)

        activeFiles = repository.activeFiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        recentFiles = repository.recentFiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openFile(file: OfficeFile) {
        _activeEditingFile.value = file
        val targetScreen = when (file.fileType) {
            "DOCUMENT" -> Screen.DocumentEditor(file.id)
            "SPREADSHEET" -> Screen.SpreadsheetEditor(file.id)
            "PRESENTATION" -> Screen.PresentationEditor(file.id)
            "PUBLISHER" -> Screen.PublisherEditor(file.id)
            "PDF" -> Screen.PdfTools(file.id)
            "FORM" -> Screen.FormEditor(file.id)
            else -> Screen.DocumentEditor(file.id)
        }
        _currentScreen.value = targetScreen
    }

    fun createNewFile(type: FileType, title: String? = null, initialContent: String? = null) {
        viewModelScope.launch {
            val docTitle = title ?: "New ${type.displayName}"
            val newId = repository.createFile(docTitle, type, initialContent)
            val created = repository.getFileById(newId)
            _activeEditingFile.value = created
            val targetScreen = when (type) {
                FileType.DOCUMENT -> Screen.DocumentEditor(newId)
                FileType.SPREADSHEET -> Screen.SpreadsheetEditor(newId)
                FileType.PRESENTATION -> Screen.PresentationEditor(newId)
                FileType.PUBLISHER -> Screen.PublisherEditor(newId)
                FileType.PDF -> Screen.PdfTools(newId)
                FileType.FORM -> Screen.FormEditor(newId)
            }
            _currentScreen.value = targetScreen
        }
    }

    fun saveCurrentFile(file: OfficeFile) {
        viewModelScope.launch {
            repository.saveFile(file)
            _activeEditingFile.value = file
        }
    }

    fun toggleFavorite(file: OfficeFile) {
        viewModelScope.launch {
            repository.toggleFavorite(file.id, file.isFavorite)
        }
    }

    fun duplicateFile(file: OfficeFile) {
        viewModelScope.launch {
            repository.duplicateFile(file)
        }
    }

    fun renameFile(file: OfficeFile, newTitle: String) {
        viewModelScope.launch {
            repository.updateFileTitle(file.id, newTitle)
        }
    }

    fun moveToTrash(file: OfficeFile) {
        viewModelScope.launch {
            repository.moveToTrash(file.id)
            if (_activeEditingFile.value?.id == file.id) {
                _currentScreen.value = Screen.Home
                _activeEditingFile.value = null
            }
        }
    }

    fun restoreFromTrash(file: OfficeFile) {
        viewModelScope.launch {
            repository.restoreFromTrash(file.id)
        }
    }

    fun deletePermanently(file: OfficeFile) {
        viewModelScope.launch {
            repository.deletePermanently(file.id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    fun switchApp(actionType: String) {
        when (actionType) {
            "DOC" -> {
                val existing = activeFiles.value.firstOrNull { it.fileType == "DOCUMENT" && !it.isInTrash }
                if (existing != null) openFile(existing) else createNewFile(FileType.DOCUMENT)
            }
            "SHEET" -> {
                val existing = activeFiles.value.firstOrNull { it.fileType == "SPREADSHEET" && !it.isInTrash }
                if (existing != null) openFile(existing) else createNewFile(FileType.SPREADSHEET)
            }
            "SLIDE" -> {
                val existing = activeFiles.value.firstOrNull { it.fileType == "PRESENTATION" && !it.isInTrash }
                if (existing != null) openFile(existing) else createNewFile(FileType.PRESENTATION)
            }
            "PUB" -> {
                val existing = activeFiles.value.firstOrNull { it.fileType == "PUBLISHER" && !it.isInTrash }
                if (existing != null) openFile(existing) else createNewFile(FileType.PUBLISHER)
            }
            "PDF" -> {
                val existing = activeFiles.value.firstOrNull { it.fileType == "PDF" && !it.isInTrash }
                if (existing != null) openFile(existing) else createNewFile(FileType.PDF)
            }
            "FORM" -> {
                val existing = activeFiles.value.firstOrNull { it.fileType == "FORM" && !it.isInTrash }
                if (existing != null) openFile(existing) else createNewFile(FileType.FORM)
            }
            "FILES" -> {
                _currentScreen.value = Screen.FileManager("ALL")
            }
            "HOME" -> {
                _currentScreen.value = Screen.Home
            }
            "SETTINGS" -> {
                _currentScreen.value = Screen.Settings
            }
        }
    }
}
