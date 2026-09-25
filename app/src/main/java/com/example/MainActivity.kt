package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.model.FileType
import com.example.ui.OfficeViewModel
import com.example.ui.drawing.DrawingScreen
import com.example.ui.editors.document.DocumentEditorScreen
import com.example.ui.editors.form.FormEditorScreen
import com.example.ui.editors.pdf.PdfToolsScreen
import com.example.ui.editors.presentation.PresentationEditorScreen
import com.example.ui.editors.publisher.PublisherEditorScreen
import com.example.ui.editors.spreadsheet.SpreadsheetEditorScreen
import com.example.ui.files.FileManagerScreen
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.Screen
import com.example.ui.scanner.ScannerScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.NovaOfficeTheme
import com.example.ui.tools.OfficeToolsScreen

class MainActivity : ComponentActivity() {

    private val viewModel: OfficeViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovaOfficeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OfficeAppRoot(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun OfficeAppRoot(viewModel: OfficeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeFiles by viewModel.activeFiles.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val activeEditingFile by viewModel.activeEditingFile.collectAsState()

    // Handle system back gesture
    BackHandler(enabled = currentScreen !is Screen.Home) {
        viewModel.navigateTo(Screen.Home)
    }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                recentFiles = recentFiles,
                allFiles = activeFiles,
                onCreateNewFile = { type -> viewModel.createNewFile(type) },
                onOpenFile = { file -> viewModel.openFile(file) },
                onToggleFavorite = { file -> viewModel.toggleFavorite(file) },
                onDuplicateFile = { file -> viewModel.duplicateFile(file) },
                onMoveToTrash = { file -> viewModel.moveToTrash(file) },
                onNavigateToFileManager = { cat -> viewModel.navigateTo(Screen.FileManager(cat)) },
                onNavigateToTools = { viewModel.navigateTo(Screen.OfficeTools()) },
                onNavigateToScanner = { viewModel.navigateTo(Screen.Scanner) },
                onNavigateToDrawing = { viewModel.navigateTo(Screen.Drawing) },
                onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) },
                onSwitchApp = { actionType -> viewModel.switchApp(actionType) }
            )
        }

        is Screen.DocumentEditor -> {
            val fileToEdit = activeEditingFile ?: activeFiles.find { it.id == screen.fileId }
            DocumentEditorScreen(
                file = fileToEdit,
                onSave = { updated -> viewModel.saveCurrentFile(updated) },
                onBack = { viewModel.navigateTo(Screen.Home) },
                onSwitchApp = { actionType -> viewModel.switchApp(actionType) }
            )
        }

        is Screen.SpreadsheetEditor -> {
            val fileToEdit = activeEditingFile ?: activeFiles.find { it.id == screen.fileId }
            SpreadsheetEditorScreen(
                file = fileToEdit,
                onSave = { updated -> viewModel.saveCurrentFile(updated) },
                onBack = { viewModel.navigateTo(Screen.Home) },
                onSwitchApp = { actionType -> viewModel.switchApp(actionType) }
            )
        }

        is Screen.PresentationEditor -> {
            val fileToEdit = activeEditingFile ?: activeFiles.find { it.id == screen.fileId }
            PresentationEditorScreen(
                file = fileToEdit,
                onSave = { updated -> viewModel.saveCurrentFile(updated) },
                onBack = { viewModel.navigateTo(Screen.Home) },
                onSwitchApp = { actionType -> viewModel.switchApp(actionType) }
            )
        }

        is Screen.PublisherEditor -> {
            val fileToEdit = activeEditingFile ?: activeFiles.find { it.id == screen.fileId }
            PublisherEditorScreen(
                file = fileToEdit,
                onSave = { updated -> viewModel.saveCurrentFile(updated) },
                onBack = { viewModel.navigateTo(Screen.Home) },
                onSwitchApp = { actionType -> viewModel.switchApp(actionType) }
            )
        }

        is Screen.PdfTools -> {
            val fileToEdit = activeEditingFile ?: activeFiles.find { it.id == screen.fileId }
            PdfToolsScreen(
                file = fileToEdit,
                onBack = { viewModel.navigateTo(Screen.Home) },
                onSwitchApp = { actionType -> viewModel.switchApp(actionType) }
            )
        }

        is Screen.FormEditor -> {
            val fileToEdit = activeEditingFile ?: activeFiles.find { it.id == screen.fileId }
            FormEditorScreen(
                file = fileToEdit,
                onSave = { updated -> viewModel.saveCurrentFile(updated) },
                onBack = { viewModel.navigateTo(Screen.Home) },
                onSwitchApp = { actionType -> viewModel.switchApp(actionType) }
            )
        }

        is Screen.FileManager -> {
            FileManagerScreen(
                files = activeFiles,
                initialCategory = screen.initialCategory,
                onOpenFile = { file -> viewModel.openFile(file) },
                onToggleFavorite = { file -> viewModel.toggleFavorite(file) },
                onDuplicateFile = { file -> viewModel.duplicateFile(file) },
                onRenameFile = { file, newTitle -> viewModel.renameFile(file, newTitle) },
                onMoveToTrash = { file -> viewModel.moveToTrash(file) },
                onRestoreFile = { file -> viewModel.restoreFromTrash(file) },
                onDeletePermanent = { file -> viewModel.deletePermanently(file) },
                onEmptyTrash = { viewModel.emptyTrash() },
                onBack = { viewModel.navigateTo(Screen.Home) },
                onSwitchApp = { actionType -> viewModel.switchApp(actionType) }
            )
        }

        is Screen.OfficeTools -> {
            OfficeToolsScreen(
                files = activeFiles,
                onConvertFile = { _, _ -> },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Scanner -> {
            ScannerScreen(
                onSaveScanAsPdf = { title, scanText ->
                    viewModel.createNewFile(
                        type = FileType.PDF,
                        title = title,
                        initialContent = "{\"pages\":[{\"pageNumber\":1,\"title\":\"Scan: $title\",\"content\":\"$scanText\"}]}"
                    )
                },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Drawing -> {
            DrawingScreen(
                onSaveDrawing = { strokeCount ->
                    viewModel.createNewFile(
                        type = FileType.DOCUMENT,
                        title = "Drawing Note #${System.currentTimeMillis() % 1000}",
                        initialContent = "{\"blocks\":[{\"type\":\"heading1\",\"text\":\"Saved Sketch (${strokeCount} strokes)\"},{\"type\":\"paragraph\",\"text\":\"Touch gesture drawing recorded.\"}]}"
                    )
                },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Settings -> {
            SettingsScreen(
                totalFilesCount = activeFiles.size,
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }
    }
}
