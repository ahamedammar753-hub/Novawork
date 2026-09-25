package com.example.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileType
import com.example.data.model.OfficeFile
import com.example.ui.components.UniversalAppSwitcherSheet
import com.example.ui.files.getFileTypeMeta
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaCobalt
import com.example.ui.theme.NovaCrimson
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaOrange
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CreateAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val fileType: FileType?,
    val actionKey: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    recentFiles: List<OfficeFile>,
    allFiles: List<OfficeFile>,
    onCreateNewFile: (FileType) -> Unit,
    onOpenFile: (OfficeFile) -> Unit,
    onToggleFavorite: (OfficeFile) -> Unit,
    onDuplicateFile: (OfficeFile) -> Unit,
    onMoveToTrash: (OfficeFile) -> Unit,
    onNavigateToFileManager: (String) -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToDrawing: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSwitchApp: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAppSwitcher by remember { mutableStateOf(false) }
    var selectedBottomNav by remember { mutableIntStateOf(0) } // 0: Home, 1: Files, 2: Tools, 3: Settings

    val createActions = listOf(
        CreateAction("Document", "Word doc", Icons.Default.Description, NovaCobalt, FileType.DOCUMENT, "NEW_DOC"),
        CreateAction("Spreadsheet", "Formulas & charts", Icons.Default.GridOn, NovaEmerald, FileType.SPREADSHEET, "NEW_SHEET"),
        CreateAction("Presentation", "Slide deck", Icons.Default.Slideshow, NovaOrange, FileType.PRESENTATION, "NEW_SLIDE"),
        CreateAction("Publisher", "Posters & flyers", Icons.Default.ViewQuilt, NovaPurple, FileType.PUBLISHER, "NEW_PUB"),
        CreateAction("PDF Document", "View & edit", Icons.Default.PictureAsPdf, NovaCrimson, FileType.PDF, "NEW_PDF"),
        CreateAction("Form / Table", "Collect data", Icons.Default.Feed, NovaTeal, FileType.FORM, "NEW_FORM")
    )

    val filteredRecents = remember(recentFiles, searchQuery) {
        if (searchQuery.isBlank()) recentFiles
        else allFiles.filter { it.title.contains(searchQuery, ignoreCase = true) && !it.isInTrash }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NovaCobalt),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("N", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NOVA Office",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NovaNavy
                            )
                            Text(
                                text = "Mobile Productivity Suite",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAppSwitcher = true },
                        modifier = Modifier.testTag("home_apps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = "Universal Apps Switcher",
                            tint = NovaCobalt,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedBottomNav == 0,
                    onClick = { selectedBottomNav = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = NovaCobalt, indicatorColor = NovaCobalt.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 1,
                    onClick = {
                        selectedBottomNav = 1
                        onNavigateToFileManager("ALL")
                    },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
                    label = { Text("Files", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 2,
                    onClick = {
                        selectedBottomNav = 2
                        onNavigateToTools()
                    },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Tools") },
                    label = { Text("Tools", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 3,
                    onClick = {
                        selectedBottomNav = 3
                        onNavigateToSettings()
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateNewFile(FileType.DOCUMENT) },
                containerColor = NovaCobalt,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("home_fab_create_doc")
            ) {
                Icon(Icons.Default.Description, contentDescription = "New Document")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search all documents, sheets, slides...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaCobalt) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("home_search_input"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            }

            // Quick Office Utilities Row (Scan, Draw/Sign, Open Existing, Tools)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionChip(
                        icon = Icons.Default.CameraAlt,
                        label = "Scan Doc",
                        color = NovaCrimson,
                        onClick = onNavigateToScanner
                    )
                    QuickActionChip(
                        icon = Icons.Default.Brush,
                        label = "Draw / Sign",
                        color = NovaPurple,
                        onClick = onNavigateToDrawing
                    )
                    QuickActionChip(
                        icon = Icons.Default.FolderOpen,
                        label = "Open File",
                        color = Color(0xFF475569),
                        onClick = { onNavigateToFileManager("ALL") }
                    )
                    QuickActionChip(
                        icon = Icons.Default.SwapHoriz,
                        label = "Converter",
                        color = NovaTeal,
                        onClick = onNavigateToTools
                    )
                }
            }

            // Create New Section (Grid of 6 core office modules)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create New",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    TextButton(onClick = { showAppSwitcher = true }) {
                        Text("View Apps", color = NovaCobalt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            item {
                // 3x2 Grid for touch-friendly cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    for (i in 0 until createActions.size step 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (j in i until minOf(i + 3, createActions.size)) {
                                val act = createActions[j]
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { act.fileType?.let { onCreateNewFile(it) } }
                                        .testTag("create_new_${act.title.lowercase()}"),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(act.color.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = act.icon,
                                                contentDescription = act.title,
                                                tint = act.color,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = act.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = act.subtitle,
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Suite Storage & Status Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Local Office Workspace",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${allFiles.size} documents saved · 100% offline & secure",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE6F4EA))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🟢 Ready",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF137333)
                            )
                        }
                    }
                }
            }

            // Recent Files Header
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "Recent Files" else "Search Results (${filteredRecents.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    TextButton(onClick = { onNavigateToFileManager("ALL") }) {
                        Text("View All", color = NovaCobalt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Recent Files List Items
            if (filteredRecents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching files found.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredRecents, key = { it.id }) { file ->
                    RecentFileItemCard(
                        file = file,
                        onOpen = { onOpenFile(file) },
                        onToggleFavorite = { onToggleFavorite(file) },
                        onDuplicate = { onDuplicateFile(file) },
                        onDelete = { onMoveToTrash(file) },
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

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Universal App Switcher Modal Sheet
    UniversalAppSwitcherSheet(
        isOpen = showAppSwitcher,
        onDismiss = { showAppSwitcher = false },
        onSelectApp = { actionType ->
            onSwitchApp(actionType)
        }
    )
}

@Composable
fun QuickActionChip(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF0F172A))
        }
    }
}

@Composable
fun RecentFileItemCard(
    file: OfficeFile,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val (icon, color) = getFileTypeMeta(file.fileType)
    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(file.lastModified))
    val sizeStr = "${(file.sizeBytes / 1024).coerceAtLeast(1)} KB"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onOpen() }
            .testTag("recent_file_${file.id}"),
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
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (file.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (file.isFavorite) Color(0xFFF59E0B) else Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        onClick = { onOpen(); menuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = { onDuplicate(); menuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = { onShare(); menuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to Trash", color = Color.Red) },
                        onClick = { onDelete(); menuExpanded = false }
                    )
                }
            }
        }
    }
}
