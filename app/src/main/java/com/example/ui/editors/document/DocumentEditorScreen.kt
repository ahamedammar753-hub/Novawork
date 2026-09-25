package com.example.ui.editors.document

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfficeFile
import com.example.ui.components.AutosaveStatusBadge
import com.example.ui.components.SaveState
import com.example.ui.components.UniversalAppSwitcherSheet
import com.example.ui.print.OfficePrintManager
import com.example.ui.theme.NovaCobalt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class DocBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    var type: String = "paragraph", // "heading1", "heading2", "paragraph", "bullet", "table"
    var text: String = "",
    var bold: Boolean = false,
    var italic: Boolean = false,
    var underline: Boolean = false,
    var align: String = "left", // "left", "center", "right"
    var fontSizeSp: Float = 16f,
    var textColorHex: String = "#0F172A",
    var highlightHex: String = "",
    var tableData: List<List<String>> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditorScreen(
    file: OfficeFile?,
    onSave: (OfficeFile) -> Unit,
    onBack: () -> Unit,
    onSwitchApp: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var docTitle by remember(file?.id) { mutableStateOf(file?.title ?: "Untitled Document") }
    val blocks = remember(file?.id) {
        mutableStateListOf<DocBlock>().apply {
            val loaded = parseDocBlocks(file?.contentJson)
            if (loaded.isNotEmpty()) {
                addAll(loaded)
            } else {
                add(DocBlock(type = "heading1", text = "Untitled Document", bold = true, fontSizeSp = 22f))
                add(DocBlock(type = "paragraph", text = "Start typing your document here...", fontSizeSp = 16f))
            }
        }
    }

    var selectedBlockIndex by remember { mutableStateOf(0) }
    var saveState by remember { mutableStateOf(SaveState.SAVED) }
    var showAppSwitcher by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showFindReplace by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var showTableDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    // Undo / Redo history
    val history = remember { mutableListOf<List<DocBlock>>() }
    var historyIndex by remember { mutableStateOf(-1) }

    fun pushHistory() {
        // Deep copy
        val snapshot = blocks.map { it.copy(tableData = it.tableData.map { r -> r.toList() }) }
        if (historyIndex < history.size - 1) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        history.add(snapshot)
        if (history.size > 25) history.removeAt(0)
        historyIndex = history.size - 1
    }

    // Auto save trigger after edits
    fun triggerAutoSave() {
        saveState = SaveState.SAVING
        scope.launch {
            delay(600)
            val json = serializeDocBlocks(docTitle, blocks)
            val updated = (file ?: OfficeFile(title = docTitle, fileType = "DOCUMENT", contentJson = json))
                .copy(title = docTitle, contentJson = json, lastModified = System.currentTimeMillis())
            onSave(updated)
            saveState = SaveState.SAVED
        }
    }

    // Word count calculation
    val wordCount = remember(blocks.map { it.text }) {
        blocks.sumOf { block ->
            block.text.split(Regex("\\s+")).count { it.isNotBlank() }
        }
    }
    val charCount = remember(blocks.map { it.text }) {
        blocks.sumOf { it.text.length }
    }

    val currentBlock = blocks.getOrNull(selectedBlockIndex)

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.clickable { showRenameDialog = true }) {
                        Text(
                            text = docTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "$wordCount words · $charCount chars",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        triggerAutoSave()
                        onBack()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AutosaveStatusBadge(saveState = saveState, onManualSave = { triggerAutoSave() })
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { showAppSwitcher = true },
                        modifier = Modifier.testTag("doc_app_switcher_button")
                    ) {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = "Apps", tint = NovaCobalt)
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Find and Replace") },
                            leadingIcon = { Icon(Icons.Default.FindReplace, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showFindReplace = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Insert Table") },
                            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showTableDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Print Document") },
                            leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                OfficePrintManager.printDocument(
                                    context = context,
                                    jobName = docTitle,
                                    title = docTitle,
                                    contentLines = blocks.map { it.text }
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share via...") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TITLE, docTitle)
                                    putExtra(Intent.EXTRA_TEXT, "$docTitle\n\n" + blocks.joinToString("\n\n") { it.text })
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Document"))
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Touch-first mobile formatting toolbar above keyboard
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo / Redo
                    IconButton(
                        onClick = {
                            if (historyIndex > 0) {
                                historyIndex--
                                val state = history[historyIndex]
                                blocks.clear()
                                blocks.addAll(state.map { it.copy() })
                                triggerAutoSave()
                            }
                        },
                        enabled = historyIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = {
                            if (historyIndex < history.size - 1) {
                                historyIndex++
                                val state = history[historyIndex]
                                blocks.clear()
                                blocks.addAll(state.map { it.copy() })
                                triggerAutoSave()
                            }
                        },
                        enabled = historyIndex < history.size - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", modifier = Modifier.size(20.dp))
                    }

                    Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.LightGray))

                    // Block Type (Title / H1 / H2 / Body)
                    val blockTypes = listOf("H1" to "heading1", "H2" to "heading2", "P" to "paragraph", "•" to "bullet")
                    blockTypes.forEach { (label, typeKey) ->
                        val isSelected = currentBlock?.type == typeKey
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NovaCobalt else Color.Transparent)
                                .clickable {
                                    pushHistory()
                                    currentBlock?.type = typeKey
                                    currentBlock?.fontSizeSp = when (typeKey) {
                                        "heading1" -> 22f
                                        "heading2" -> 18f
                                        else -> 16f
                                    }
                                    if (typeKey == "heading1") currentBlock?.bold = true
                                    triggerAutoSave()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.LightGray))

                    // Bold
                    IconButton(
                        onClick = {
                            pushHistory()
                            currentBlock?.let { it.bold = !it.bold }
                            triggerAutoSave()
                        }
                    ) {
                        Icon(
                            Icons.Default.FormatBold,
                            contentDescription = "Bold",
                            tint = if (currentBlock?.bold == true) NovaCobalt else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Italic
                    IconButton(
                        onClick = {
                            pushHistory()
                            currentBlock?.let { it.italic = !it.italic }
                            triggerAutoSave()
                        }
                    ) {
                        Icon(
                            Icons.Default.FormatItalic,
                            contentDescription = "Italic",
                            tint = if (currentBlock?.italic == true) NovaCobalt else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Underline
                    IconButton(
                        onClick = {
                            pushHistory()
                            currentBlock?.let { it.underline = !it.underline }
                            triggerAutoSave()
                        }
                    ) {
                        Icon(
                            Icons.Default.FormatUnderlined,
                            contentDescription = "Underline",
                            tint = if (currentBlock?.underline == true) NovaCobalt else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Alignment
                    IconButton(
                        onClick = {
                            pushHistory()
                            currentBlock?.let {
                                it.align = when (it.align) {
                                    "left" -> "center"
                                    "center" -> "right"
                                    else -> "left"
                                }
                            }
                            triggerAutoSave()
                        }
                    ) {
                        Icon(
                            when (currentBlock?.align) {
                                "center" -> Icons.Default.FormatAlignCenter
                                "right" -> Icons.AutoMirrored.Filled.FormatAlignRight
                                else -> Icons.AutoMirrored.Filled.FormatAlignLeft
                            },
                            contentDescription = "Alignment"
                        )
                    }

                    // Add Paragraph Block
                    IconButton(
                        onClick = {
                            pushHistory()
                            blocks.add(DocBlock(type = "paragraph", text = "", fontSizeSp = 16f))
                            selectedBlockIndex = blocks.size - 1
                            triggerAutoSave()
                        }
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = "Add Block", tint = NovaCobalt)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            // Find and Replace panel
            AnimatedVisibility(visible = showFindReplace) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = findQuery,
                                onValueChange = { findQuery = it },
                                label = { Text("Find") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = replaceQuery,
                                onValueChange = { replaceQuery = it },
                                label = { Text("Replace") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = { showFindReplace = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = {
                                    if (findQuery.isNotBlank()) {
                                        pushHistory()
                                        blocks.forEach { b ->
                                            b.text = b.text.replace(findQuery, replaceQuery, ignoreCase = true)
                                        }
                                        triggerAutoSave()
                                    }
                                }
                            ) {
                                Text("Replace All")
                            }
                        }
                    }
                }
            }

            // Document Page Canvas
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    blocks.forEachIndexed { index, block ->
                        val isSelected = selectedBlockIndex == index

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) NovaCobalt.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(4.dp)
                                .clickable { selectedBlockIndex = index }
                        ) {
                            if (block.type == "table" && block.tableData.isNotEmpty()) {
                                TableBlockView(block.tableData)
                            } else {
                                val textStyle = TextStyle(
                                    fontSize = block.fontSizeSp.sp,
                                    fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = if (block.underline) TextDecoration.Underline else TextDecoration.None,
                                    textAlign = when (block.align) {
                                        "center" -> TextAlign.Center
                                        "right" -> TextAlign.Right
                                        else -> TextAlign.Start
                                    },
                                    color = Color(0xFF0F172A)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                    if (block.type == "bullet") {
                                        Text("• ", style = textStyle, fontWeight = FontWeight.Bold)
                                    }
                                    BasicTextField(
                                        value = block.text,
                                        onValueChange = {
                                            block.text = it
                                            triggerAutoSave()
                                        },
                                        textStyle = textStyle,
                                        modifier = Modifier.fillMaxWidth().testTag("doc_text_block_$index")
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }

    // Insert Table Dialog
    if (showTableDialog) {
        var rowsInput by remember { mutableStateOf("3") }
        var colsInput by remember { mutableStateOf("3") }

        AlertDialog(
            onDismissRequest = { showTableDialog = false },
            title = { Text("Insert Table") },
            text = {
                Column {
                    OutlinedTextField(
                        value = rowsInput,
                        onValueChange = { rowsInput = it },
                        label = { Text("Rows (1-10)") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = colsInput,
                        onValueChange = { colsInput = it },
                        label = { Text("Columns (1-5)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val r = rowsInput.toIntOrNull()?.coerceIn(1, 10) ?: 3
                    val c = colsInput.toIntOrNull()?.coerceIn(1, 5) ?: 3
                    val sampleData = List(r) { rowIdx ->
                        List(c) { colIdx ->
                            if (rowIdx == 0) "Header ${colIdx + 1}" else "Data $rowIdx-$colIdx"
                        }
                    }
                    pushHistory()
                    blocks.add(DocBlock(type = "table", tableData = sampleData))
                    triggerAutoSave()
                    showTableDialog = false
                }) {
                    Text("Insert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTableDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Rename Document Dialog
    if (showRenameDialog) {
        var tempTitle by remember { mutableStateOf(docTitle) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = tempTitle,
                    onValueChange = { tempTitle = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempTitle.isNotBlank()) {
                        docTitle = tempTitle
                        triggerAutoSave()
                    }
                    showRenameDialog = false
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Universal App Switcher Bottom Sheet
    UniversalAppSwitcherSheet(
        isOpen = showAppSwitcher,
        onDismiss = { showAppSwitcher = false },
        onSelectApp = { actionType ->
            triggerAutoSave()
            onSwitchApp(actionType)
        }
    )
}

@Composable
fun TableBlockView(table: List<List<String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
    ) {
        table.forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (rowIdx == 0) Color(0xFFF1F5F9) else Color.White)
            ) {
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color(0xFFE2E8F0))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = cell,
                            fontSize = 13.sp,
                            fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }
    }
}

private fun parseDocBlocks(jsonStr: String?): List<DocBlock> {
    if (jsonStr.isNullOrBlank()) return emptyList()
    val list = mutableListOf<DocBlock>()
    try {
        val root = JSONObject(jsonStr)
        val arr = root.optJSONArray("blocks") ?: return emptyList()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val type = obj.optString("type", "paragraph")
            val text = obj.optString("text", "")
            val bold = obj.optBoolean("bold", false)
            val italic = obj.optBoolean("italic", false)
            val align = obj.optString("align", "left")
            val fontSize = when (type) {
                "heading1" -> 22f
                "heading2" -> 18f
                else -> 16f
            }
            list.add(DocBlock(type = type, text = text, bold = bold, italic = italic, align = align, fontSizeSp = fontSize))
        }
    } catch (_: Exception) {}
    return list
}

private fun serializeDocBlocks(title: String, blocks: List<DocBlock>): String {
    val root = JSONObject()
    root.put("title", title)
    val arr = JSONArray()
    blocks.forEach { b ->
        val obj = JSONObject()
        obj.put("type", b.type)
        obj.put("text", b.text)
        obj.put("bold", b.bold)
        obj.put("italic", b.italic)
        obj.put("align", b.align)
        arr.put(obj)
    }
    root.put("blocks", arr)
    return root.toString()
}
