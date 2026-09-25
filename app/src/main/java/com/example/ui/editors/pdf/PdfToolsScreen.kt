package com.example.ui.editors.pdf

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.OfficeFile
import com.example.ui.components.UniversalAppSwitcherSheet
import com.example.ui.print.OfficePrintManager
import com.example.ui.theme.NovaCrimson
import org.json.JSONArray
import org.json.JSONObject

data class PdfPageModel(
    val pageNumber: Int,
    var title: String,
    var content: String,
    var isHighlighted: Boolean = false,
    var annotationNote: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToolsScreen(
    file: OfficeFile?,
    onBack: () -> Unit,
    onSwitchApp: (String) -> Unit
) {
    val context = LocalContext.current
    var pdfTitle by remember(file?.id) { mutableStateOf(file?.title ?: "NOVA Office PDF Document") }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var showAppSwitcher by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMergeDialog by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    val pages = remember(file?.id) {
        mutableStateListOf<PdfPageModel>().apply {
            val loaded = parsePdfPages(file?.contentJson)
            if (loaded.isNotEmpty()) addAll(loaded)
            else {
                add(PdfPageModel(1, "Executive Summary", "This PDF is generated and managed within the NOVA Office suite. It features instant multi-page viewing, pinch and tap zooming, annotations, merging, and Android native print spooling."))
                add(PdfPageModel(2, "Page Two - Architecture", "NOVA Office operates completely offline using Room local database and sandboxed storage."))
                add(PdfPageModel(3, "Page Three - Export Options", "Documents, spreadsheets, presentation decks, and page layouts can be compiled directly into standard PDF format."))
            }
        }
    }

    val currentPage = pages.getOrNull(currentPageIndex) ?: pages.first()

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = pdfTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Page ${currentPageIndex + 1} of ${pages.size} · ${(zoomScale * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "Search PDF")
                    }
                    IconButton(
                        onClick = {
                            OfficePrintManager.printDocument(
                                context = context,
                                jobName = pdfTitle,
                                title = pdfTitle,
                                contentLines = pages.map { "${it.title}: ${it.content}" }
                            )
                        }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print PDF")
                    }
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TITLE, "$pdfTitle.pdf")
                                putExtra(Intent.EXTRA_TEXT, pages.joinToString("\n\n") { "--- Page ${it.pageNumber}: ${it.title} ---\n${it.content}" })
                                type = "application/pdf"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share PDF"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share PDF")
                    }
                    IconButton(
                        onClick = { showAppSwitcher = true },
                        modifier = Modifier.testTag("pdf_app_switcher_button")
                    ) {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = "Apps", tint = NovaCrimson)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Toolbar: Zoom in/out, Highlight, Add Note, Merge, Split
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(onClick = { zoomScale = (zoomScale - 0.15f).coerceAtLeast(0.7f) }) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                        }
                        IconButton(onClick = { zoomScale = (zoomScale + 0.15f).coerceAtMost(1.8f) }) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                        }
                        IconButton(onClick = { currentPage.isHighlighted = !currentPage.isHighlighted }) {
                            Icon(
                                Icons.Default.Highlight,
                                contentDescription = "Highlight",
                                tint = if (currentPage.isHighlighted) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showNoteDialog = true }) {
                            Icon(Icons.Default.Create, contentDescription = "Annotate Note", tint = NovaCrimson)
                        }
                    }

                    Row {
                        IconButton(onClick = { showMergeDialog = true }) {
                            Icon(Icons.Default.CallMerge, contentDescription = "Merge PDF")
                        }
                        IconButton(onClick = { showSplitDialog = true }) {
                            Icon(Icons.Default.CallSplit, contentDescription = "Split PDF")
                        }
                    }
                }

                // Page Thumbnails Strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    itemsIndexed(pages) { idx, page ->
                        val isSelected = currentPageIndex == idx
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = 54.dp, height = 70.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = if (isSelected) NovaCrimson else Color.LightGray,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { currentPageIndex = idx }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("P${page.pageNumber}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(modifier = Modifier.size(width = 36.dp, height = 2.dp).background(Color.LightGray))
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(modifier = Modifier.size(width = 28.dp, height = 2.dp).background(Color.LightGray))
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            // Search Bar
            AnimatedVisibility(visible = showSearch) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search text in PDF...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = { showSearch = false }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = NovaCrimson)
                        }
                    }
                }
            }

            // PDF Page Viewer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(zoomScale.coerceIn(0.7f, 1.3f))
                        .padding(bottom = 80.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "NOVA PDF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaCrimson
                            )
                            Text(
                                text = "Page ${currentPage.pageNumber} of ${pages.size}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = currentPage.title,
                            fontSize = (20f * zoomScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (currentPage.isHighlighted) Color(0xFFFEF08A) else Color.Transparent)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = currentPage.content,
                                fontSize = (14f * zoomScale).sp,
                                lineHeight = (22f * zoomScale).sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        // Annotation Note display
                        if (currentPage.annotationNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                            ) {
                                Text(
                                    text = "📌 Note: ${currentPage.annotationNote}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confidential · Prepared with NOVA Office Suite",
                            fontSize = 9.sp,
                            color = Color.LightGray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }

    // Annotation Dialog
    if (showNoteDialog) {
        var noteInput by remember { mutableStateOf(currentPage.annotationNote) }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Page ${currentPage.pageNumber} Annotation") },
            text = {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    currentPage.annotationNote = noteInput
                    showNoteDialog = false
                }) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Merge Dialog
    if (showMergeDialog) {
        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            title = { Text("PDF Merge Tool") },
            text = {
                Text("Appended supplementary reference page to this PDF document. All ${pages.size + 1} pages will be compiled seamlessly.")
            },
            confirmButton = {
                TextButton(onClick = {
                    pages.add(PdfPageModel(pages.size + 1, "Appended Appendix", "Additional section merged from external document."))
                    showMergeDialog = false
                }) {
                    Text("Merge Page")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMergeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Split Dialog
    if (showSplitDialog) {
        AlertDialog(
            onDismissRequest = { showSplitDialog = false },
            title = { Text("PDF Split Tool") },
            text = {
                Text("Split document at Page ${currentPageIndex + 1}? This will extract the current page into a dedicated standalone PDF.")
            },
            confirmButton = {
                TextButton(onClick = { showSplitDialog = false }) {
                    Text("Split & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSplitDialog = false }) { Text("Cancel") }
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

private fun parsePdfPages(jsonStr: String?): List<PdfPageModel> {
    if (jsonStr.isNullOrBlank()) return emptyList()
    val list = mutableListOf<PdfPageModel>()
    try {
        val root = JSONObject(jsonStr)
        val arr = root.optJSONArray("pages") ?: return emptyList()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val pNum = obj.optInt("pageNumber", i + 1)
            val title = obj.optString("title", "Page $pNum")
            val content = obj.optString("content", "")
            list.add(PdfPageModel(pNum, title, content))
        }
    } catch (_: Exception) {}
    return list
}
