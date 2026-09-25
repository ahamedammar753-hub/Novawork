package com.example.ui.files

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileType
import com.example.data.model.OfficeFile
import com.example.ui.components.UniversalAppSwitcherSheet
import com.example.ui.theme.NovaCobalt
import com.example.ui.theme.NovaCrimson
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaOrange
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    files: List<OfficeFile>,
    initialCategory: String = "ALL",
    onOpenFile: (OfficeFile) -> Unit,
    onToggleFavorite: (OfficeFile) -> Unit,
    onDuplicateFile: (OfficeFile) -> Unit,
    onRenameFile: (OfficeFile, String) -> Unit,
    onMoveToTrash: (OfficeFile) -> Unit,
    onRestoreFile: (OfficeFile) -> Unit,
    onDeletePermanent: (OfficeFile) -> Unit,
    onEmptyTrash: () -> Unit,
    onBack: () -> Unit,
    onSwitchApp: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var isGridView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("DATE") } // "DATE", "NAME", "SIZE", "TYPE"
    var showSortMenu by remember { mutableStateOf(false) }
    var showAppSwitcher by remember { mutableStateOf(false) }

    var fileToRename by remember { mutableStateOf<OfficeFile?>(null) }
    var newRenameTitle by remember { mutableStateOf("") }

    val categories = listOf(
        "ALL" to "All Files",
        "DOCUMENT" to "Documents",
        "SPREADSHEET" to "Spreadsheets",
        "PRESENTATION" to "Presentations",
        "PUBLISHER" to "Publisher",
        "PDF" to "PDFs",
        "FORM" to "Forms",
        "FAVORITES" to "Favorites",
        "TRASH" to "Trash"
    )

    // Filter files
    val filteredFiles = files.filter { file ->
        val matchesCategory = when (selectedCategory) {
            "ALL" -> !file.isInTrash
            "FAVORITES" -> file.isFavorite && !file.isInTrash
            "TRASH" -> file.isInTrash
            else -> file.fileType == selectedCategory && !file.isInTrash
        }
        val matchesSearch = searchQuery.isBlank() || file.title.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }.let { list ->
        when (sortBy) {
            "NAME" -> list.sortedBy { it.title.lowercase(Locale.US) }
            "SIZE" -> list.sortedByDescending { it.sizeBytes }
            "TYPE" -> list.sortedBy { it.fileType }
            else -> list.sortedByDescending { it.lastModified }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("File Manager", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${filteredFiles.size} items · $selectedCategory",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle View"
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(text = { Text("Sort by Date (Newest)") }, onClick = { sortBy = "DATE"; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Sort by Name (A-Z)") }, onClick = { sortBy = "NAME"; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Sort by Size (Largest)") }, onClick = { sortBy = "SIZE"; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Sort by Type") }, onClick = { sortBy = "TYPE"; showSortMenu = false })
                        }
                    }
                    if (selectedCategory == "TRASH" && filteredFiles.isNotEmpty()) {
                        IconButton(onClick = onEmptyTrash) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Empty Trash", tint = Color(0xFFEF4444))
                        }
                    }
                    IconButton(
                        onClick = { showAppSwitcher = true },
                        modifier = Modifier.testTag("file_mgr_app_switcher_button")
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = "Apps", tint = NovaCobalt)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search files by title...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            // Category Chips Row
            ScrollableTabRow(
                selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
                edgePadding = 16.dp,
                divider = {}
            ) {
                categories.forEach { (catKey, catLabel) ->
                    Tab(
                        selected = selectedCategory == catKey,
                        onClick = { selectedCategory = catKey },
                        text = {
                            Text(
                                text = catLabel,
                                fontWeight = if (selectedCategory == catKey) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No files in $selectedCategory", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredFiles, key = { it.id }) { file ->
                        FileGridCard(
                            file = file,
                            onOpen = { onOpenFile(file) },
                            onToggleFavorite = { onToggleFavorite(file) },
                            onDuplicate = { onDuplicateFile(file) },
                            onRename = {
                                fileToRename = file
                                newRenameTitle = file.title
                            },
                            onDelete = { onMoveToTrash(file) },
                            onRestore = { onRestoreFile(file) },
                            onPermanentDelete = { onDeletePermanent(file) },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TITLE, file.title)
                                    putExtra(Intent.EXTRA_TEXT, file.contentJson)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share ${file.title}"))
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    items(filteredFiles, key = { it.id }) { file ->
                        FileListRow(
                            file = file,
                            onOpen = { onOpenFile(file) },
                            onToggleFavorite = { onToggleFavorite(file) },
                            onDuplicate = { onDuplicateFile(file) },
                            onRename = {
                                fileToRename = file
                                newRenameTitle = file.title
                            },
                            onDelete = { onMoveToTrash(file) },
                            onRestore = { onRestoreFile(file) },
                            onPermanentDelete = { onDeletePermanent(file) },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TITLE, file.title)
                                    putExtra(Intent.EXTRA_TEXT, file.contentJson)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share ${file.title}"))
                            }
                        )
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (fileToRename != null) {
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = newRenameTitle,
                    onValueChange = { newRenameTitle = it },
                    label = { Text("New file name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    fileToRename?.let { f ->
                        if (newRenameTitle.isNotBlank()) onRenameFile(f, newRenameTitle)
                    }
                    fileToRename = null
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) { Text("Cancel") }
            }
        )
    }

    // Universal App Switcher Bottom Sheet
    UniversalAppSwitcherSheet(
        isOpen = showAppSwitcher,
        onDismiss = { showAppSwitcher = false },
        onSelectApp = onSwitchApp
    )
}

@Composable
fun FileListRow(
    file: OfficeFile,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    onShare: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val (icon, color) = getFileTypeMeta(file.fileType)
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(file.lastModified))
    val sizeStr = "${(file.sizeBytes / 1024).coerceAtLeast(1)} KB"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onOpen() }
            .testTag("file_item_${file.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = file.fileType, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateStr · $sizeStr",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (!file.isInTrash) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (file.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (file.isFavorite) Color(0xFFF59E0B) else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Actions", modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    if (file.isInTrash) {
                        DropdownMenuItem(
                            text = { Text("Restore") },
                            leadingIcon = { Icon(Icons.Default.RestoreFromTrash, contentDescription = null) },
                            onClick = { onRestore(); menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Forever", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red) },
                            onClick = { onPermanentDelete(); menuExpanded = false }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = { onRename(); menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = { onDuplicate(); menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = { onShare(); menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to Trash", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                            onClick = { onDelete(); menuExpanded = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileGridCard(
    file: OfficeFile,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    onShare: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val (icon, color) = getFileTypeMeta(file.fileType)
    val dateStr = SimpleDateFormat("MMM d", Locale.US).format(Date(file.lastModified))

    Card(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        if (file.isInTrash) {
                            DropdownMenuItem(text = { Text("Restore") }, onClick = { onRestore(); menuExpanded = false })
                            DropdownMenuItem(text = { Text("Delete Forever", color = Color.Red) }, onClick = { onPermanentDelete(); menuExpanded = false })
                        } else {
                            DropdownMenuItem(text = { Text("Rename") }, onClick = { onRename(); menuExpanded = false })
                            DropdownMenuItem(text = { Text("Duplicate") }, onClick = { onDuplicate(); menuExpanded = false })
                            DropdownMenuItem(text = { Text("Share") }, onClick = { onShare(); menuExpanded = false })
                            DropdownMenuItem(text = { Text("Trash", color = Color.Red) }, onClick = { onDelete(); menuExpanded = false })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = file.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 2,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateStr, fontSize = 11.sp, color = Color(0xFF64748B))
                if (!file.isInTrash) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (file.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (file.isFavorite) Color(0xFFF59E0B) else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getFileTypeMeta(typeStr: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (typeStr) {
        "DOCUMENT" -> Icons.Default.Description to NovaCobalt
        "SPREADSHEET" -> Icons.Default.TableChart to NovaEmerald
        "PRESENTATION" -> Icons.Default.Slideshow to NovaOrange
        "PUBLISHER" -> Icons.Default.ViewQuilt to NovaPurple
        "PDF" -> Icons.Default.PictureAsPdf to NovaCrimson
        "FORM" -> Icons.Default.Feed to NovaTeal
        else -> Icons.Default.Description to Color.Gray
    }
}
