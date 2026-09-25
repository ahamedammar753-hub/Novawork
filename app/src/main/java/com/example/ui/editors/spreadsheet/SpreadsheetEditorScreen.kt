package com.example.ui.editors.spreadsheet

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SortByAlpha
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfficeFile
import com.example.ui.components.AutosaveStatusBadge
import com.example.ui.components.SaveState
import com.example.ui.components.UniversalAppSwitcherSheet
import com.example.ui.print.OfficePrintManager
import com.example.ui.theme.NovaEmerald
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class CellData(
    val value: String = "",
    val bold: Boolean = false,
    val italic: Boolean = false,
    val format: String = "General", // "General", "Currency", "Percentage", "Decimal"
    val bg: String = ""
)

data class SheetModel(
    val name: String,
    val rowCount: Int = 30,
    val colCount: Int = 10,
    val cells: MutableMap<String, CellData> = mutableMapOf()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpreadsheetEditorScreen(
    file: OfficeFile?,
    onSave: (OfficeFile) -> Unit,
    onBack: () -> Unit,
    onSwitchApp: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sheetTitle by remember(file?.id) { mutableStateOf(file?.title ?: "Untitled Spreadsheet") }
    var activeSheetIndex by remember { mutableIntStateOf(0) }
    val sheets = remember(file?.id) {
        val parsed = parseSpreadsheetContent(file?.contentJson)
        if (parsed.isNotEmpty()) parsed else mutableListOf(
            SheetModel("Sheet1", 30, 8),
            SheetModel("Sheet2", 20, 6)
        )
    }

    var selectedCellKey by remember { mutableStateOf("A1") }
    var formulaBarText by remember { mutableStateOf("") }
    var saveState by remember { mutableStateOf(SaveState.SAVED) }
    var showAppSwitcher by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showChartDialog by remember { mutableStateOf(false) }
    var chartType by remember { mutableStateOf("COLUMN") } // "COLUMN", "BAR", "LINE", "PIE"
    var showAddSheetDialog by remember { mutableStateOf(false) }
    var showRenameSheetDialog by remember { mutableStateOf(false) }

    val currentSheet = sheets.getOrNull(activeSheetIndex) ?: sheets.first()

    // Sync formula bar when selected cell changes
    LaunchedEffect(selectedCellKey, activeSheetIndex) {
        formulaBarText = currentSheet.cells[selectedCellKey]?.value ?: ""
    }

    fun triggerAutoSave() {
        saveState = SaveState.SAVING
        scope.launch {
            delay(600)
            val json = serializeSpreadsheet(sheets, activeSheetIndex)
            val updated = (file ?: OfficeFile(title = sheetTitle, fileType = "SPREADSHEET", contentJson = json))
                .copy(title = sheetTitle, contentJson = json, lastModified = System.currentTimeMillis())
            onSave(updated)
            saveState = SaveState.SAVED
        }
    }

    fun getCellValue(coord: String): String {
        return currentSheet.cells[coord]?.value ?: ""
    }

    fun setCellValue(coord: String, rawVal: String) {
        val existing = currentSheet.cells[coord] ?: CellData()
        currentSheet.cells[coord] = existing.copy(value = rawVal)
        triggerAutoSave()
    }

    fun getEvaluatedDisplay(coord: String): String {
        val raw = getCellValue(coord)
        if (raw.isBlank()) return ""
        val eval = FormulaEngine.evaluate(raw) { c -> getCellValue(c) }
        val cell = currentSheet.cells[coord]
        return formatDisplayValue(eval, cell?.format ?: "General")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = sheetTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Cell $selectedCellKey = ${getEvaluatedDisplay(selectedCellKey)}",
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
                        onClick = { showChartDialog = true },
                        modifier = Modifier.testTag("sheet_charts_button")
                    ) {
                        Icon(imageVector = Icons.Default.BarChart, contentDescription = "Charts", tint = NovaEmerald)
                    }
                    IconButton(
                        onClick = { showAppSwitcher = true },
                        modifier = Modifier.testTag("sheet_app_switcher_button")
                    ) {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = "Apps", tint = NovaEmerald)
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Sort Column A-Z") },
                            leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                // Sort rows 2..rowCount by column of selectedCell
                                val colLetter = selectedCellKey.filter { it.isLetter() }
                                sortCurrentSheetColumn(currentSheet, colLetter, ascending = true)
                                triggerAutoSave()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Print Sheet") },
                            leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val lines = mutableListOf<String>()
                                for (r in 1..minOf(currentSheet.rowCount, 15)) {
                                    val rowVals = (0 until minOf(currentSheet.colCount, 5)).map { c ->
                                        val key = "${('A'.code + c).toChar()}$r"
                                        getEvaluatedDisplay(key)
                                    }
                                    lines.add(rowVals.joinToString("  |  "))
                                }
                                OfficePrintManager.printDocument(context, sheetTitle, currentSheet.name, lines)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export to CSV / Share") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val sb = java.lang.StringBuilder()
                                for (r in 1..currentSheet.rowCount) {
                                    val rowVals = (0 until currentSheet.colCount).map { c ->
                                        val key = "${('A'.code + c).toChar()}$r"
                                        "\"${getEvaluatedDisplay(key).replace("\"", "\"\"")}\""
                                    }
                                    sb.append(rowVals.joinToString(",")).append("\n")
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TITLE, "$sheetTitle.csv")
                                    putExtra(Intent.EXTRA_TEXT, sb.toString())
                                    type = "text/csv"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Spreadsheet CSV"))
                            }
                        )
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

                // Quick Formula Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NovaEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "fx $selectedCellKey",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NovaEmerald
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = formulaBarText,
                        onValueChange = {
                            formulaBarText = it
                            setCellValue(selectedCellKey, it)
                        },
                        textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("formula_input_field")
                    )
                    IconButton(
                        onClick = {
                            setCellValue(selectedCellKey, formulaBarText)
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Apply", tint = NovaEmerald)
                    }
                }

                // Quick formula shortcut chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    val formulas = listOf("SUM", "AVERAGE", "MIN", "MAX", "COUNT", "IF", "ROUND", "CONCAT")
                    formulas.forEach { fn ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    val col = selectedCellKey.filter { it.isLetter() }
                                    val row = selectedCellKey.filter { it.isDigit() }.toIntOrNull() ?: 1
                                    val defaultArg = if (row > 1) "$col" + "1:$col${row - 1}" else "A1:A10"
                                    val insertText = "=$fn($defaultArg)"
                                    formulaBarText = insertText
                                    setCellValue(selectedCellKey, insertText)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = fn, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NovaEmerald)
                        }
                    }

                    // Format chips ($ , % , .00)
                    val formats = listOf("$" to "Currency", "%" to "Percentage", ".00" to "Decimal", "ABC" to "General")
                    formats.forEach { (label, fmt) ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE2E8F0))
                                .clickable {
                                    val curr = currentSheet.cells[selectedCellKey] ?: CellData()
                                    currentSheet.cells[selectedCellKey] = curr.copy(format = fmt)
                                    triggerAutoSave()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        }
                    }

                    // Bold toggle
                    IconButton(
                        onClick = {
                            val curr = currentSheet.cells[selectedCellKey] ?: CellData()
                            currentSheet.cells[selectedCellKey] = curr.copy(bold = !curr.bold)
                            triggerAutoSave()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp))
                    }
                }

                // Sheets Bar (Tab row)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = activeSheetIndex,
                        modifier = Modifier.weight(1f),
                        edgePadding = 8.dp
                    ) {
                        sheets.forEachIndexed { index, sheet ->
                            Tab(
                                selected = activeSheetIndex == index,
                                onClick = { activeSheetIndex = index },
                                text = { Text(sheet.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                    IconButton(onClick = { showAddSheetDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Sheet", tint = NovaEmerald)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Horizontal scroll container for the grid
            val horizontalScrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                // Header row + rows
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Column Headers (Empty corner + A, B, C, D...)
                    item {
                        Row(modifier = Modifier.background(Color(0xFFE2E8F0))) {
                            // Top left corner cell
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 32.dp)
                                    .border(0.5.dp, Color(0xFFCBD5E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            }
                            // Column letters A to ...
                            for (c in 0 until currentSheet.colCount) {
                                val colLetter = ('A'.code + c).toChar().toString()
                                Box(
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 32.dp)
                                        .border(0.5.dp, Color(0xFFCBD5E1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(colLetter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                }
                            }
                        }
                    }

                    // Grid Data Rows
                    items(currentSheet.rowCount) { rowIdx ->
                        val rowNum = rowIdx + 1
                        Row {
                            // Row Number Header cell
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 36.dp)
                                    .background(Color(0xFFF1F5F9))
                                    .border(0.5.dp, Color(0xFFCBD5E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(rowNum.toString(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            }

                            // Individual Cells
                            for (c in 0 until currentSheet.colCount) {
                                val colLetter = ('A'.code + c).toChar().toString()
                                val coord = "$colLetter$rowNum"
                                val isSelected = selectedCellKey == coord
                                val cellData = currentSheet.cells[coord]
                                val displayVal = getEvaluatedDisplay(coord)

                                val cellBg = when {
                                    isSelected -> Color(0xFFD1FAE5)
                                    !cellData?.bg.isNullOrBlank() -> try {
                                        Color(android.graphics.Color.parseColor(cellData!!.bg))
                                    } catch (_: Exception) { Color.White }
                                    else -> Color.White
                                }

                                Box(
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 36.dp)
                                        .background(cellBg)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.5.dp,
                                            color = if (isSelected) NovaEmerald else Color(0xFFE2E8F0)
                                        )
                                        .clickable { selectedCellKey = coord }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = displayVal,
                                        fontSize = 12.sp,
                                        fontWeight = if (cellData?.bold == true) FontWeight.Bold else FontWeight.Normal,
                                        fontStyle = if (cellData?.italic == true) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Chart Dialog
    if (showChartDialog) {
        AlertDialog(
            onDismissRequest = { showChartDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = NovaEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Interactive Sheet Chart")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val types = listOf("COLUMN" to "Column", "BAR" to "Bar", "LINE" to "Line", "PIE" to "Pie")
                        types.forEach { (tKey, tLabel) ->
                            val active = chartType == tKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) NovaEmerald else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { chartType = tKey }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Extract first non-empty column numbers from sheet
                    val chartPoints = remember(currentSheet.cells.size, chartType) {
                        val points = mutableListOf<Pair<String, Float>>()
                        for (r in 2..minOf(currentSheet.rowCount, 10)) {
                            val label = getEvaluatedDisplay("A$r").ifBlank { "R$r" }
                            val numStr = getEvaluatedDisplay("B$r").replace("$", "").replace(",", "").replace("%", "").trim()
                            val num = numStr.toFloatOrNull() ?: (r * 15f)
                            points.add(label to num)
                        }
                        points
                    }

                    // Render chart on Canvas
                    SpreadsheetCanvasChart(chartType = chartType, data = chartPoints)
                }
            },
            confirmButton = {
                TextButton(onClick = { showChartDialog = false }) { Text("Close") }
            }
        )
    }

    // Add Sheet Dialog
    if (showAddSheetDialog) {
        var newSheetName by remember { mutableStateOf("Sheet${sheets.size + 1}") }
        AlertDialog(
            onDismissRequest = { showAddSheetDialog = false },
            title = { Text("New Sheet") },
            text = {
                OutlinedTextField(
                    value = newSheetName,
                    onValueChange = { newSheetName = it },
                    label = { Text("Sheet Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSheetName.isNotBlank()) {
                        sheets.add(SheetModel(newSheetName, 30, 8))
                        activeSheetIndex = sheets.size - 1
                        triggerAutoSave()
                    }
                    showAddSheetDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSheetDialog = false }) { Text("Cancel") }
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
fun SpreadsheetCanvasChart(chartType: String, data: List<Pair<String, Float>>) {
    val maxVal = data.maxOfOrNull { it.second }?.coerceAtLeast(10f) ?: 100f
    val colors = listOf(
        Color(0xFF2563EB), Color(0xFF059669), Color(0xFFEA580C),
        Color(0xFF7C3AED), Color(0xFF0D9488), Color(0xFFE11D48)
    )

    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val w = size.width
            val h = size.height
            val bottomY = h - 25f

            when (chartType) {
                "COLUMN" -> {
                    val barWidth = (w / (data.size * 1.5f)).coerceAtLeast(16f)
                    data.forEachIndexed { idx, pair ->
                        val fraction = (pair.second / maxVal).coerceIn(0f, 1f)
                        val barHeight = fraction * (bottomY - 20f)
                        val x = idx * (barWidth * 1.4f) + 15f
                        val y = bottomY - barHeight
                        val color = colors[idx % colors.size]

                        drawRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
                "BAR" -> {
                    val barHeight = ((h - 20f) / data.size).coerceAtLeast(14f)
                    data.forEachIndexed { idx, pair ->
                        val fraction = (pair.second / maxVal).coerceIn(0f, 1f)
                        val barW = fraction * (w - 40f)
                        val y = idx * barHeight + 5f
                        val color = colors[idx % colors.size]

                        drawRect(
                            color = color,
                            topLeft = Offset(10f, y),
                            size = Size(barW, barHeight - 4f)
                        )
                    }
                }
                "LINE" -> {
                    if (data.size > 1) {
                        val stepX = (w - 30f) / (data.size - 1)
                        for (i in 0 until data.size - 1) {
                            val f1 = (data[i].second / maxVal).coerceIn(0f, 1f)
                            val f2 = (data[i + 1].second / maxVal).coerceIn(0f, 1f)
                            val p1 = Offset(15f + i * stepX, bottomY - f1 * (bottomY - 20f))
                            val p2 = Offset(15f + (i + 1) * stepX, bottomY - f2 * (bottomY - 20f))

                            drawLine(
                                color = NovaEmerald,
                                start = p1,
                                end = p2,
                                strokeWidth = 5f
                            )
                            drawCircle(color = Color(0xFF0F172A), radius = 6f, center = p1)
                        }
                    }
                }
                "PIE" -> {
                    val total = data.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
                    var startAngle = 0f
                    val radius = minOf(w, h) / 2.5f
                    val center = Offset(w / 2, h / 2)

                    data.forEachIndexed { idx, pair ->
                        val sweep = (pair.second / total) * 360f
                        drawArc(
                            color = colors[idx % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                        startAngle += sweep
                    }
                }
            }
        }
    }
}

private fun formatDisplayValue(raw: String, format: String): String {
    if (raw.isBlank() || raw.startsWith("#")) return raw
    val num = raw.replace("$", "").replace(",", "").toDoubleOrNull() ?: return raw
    return when (format) {
        "Currency" -> String.format(Locale.US, "$%,.2f", num)
        "Percentage" -> String.format(Locale.US, "%.1f%%", num)
        "Decimal" -> String.format(Locale.US, "%.2f", num)
        else -> if (num == num.toLong().toDouble()) num.toLong().toString() else raw
    }
}

private fun sortCurrentSheetColumn(sheet: SheetModel, colLetter: String, ascending: Boolean) {
    val rows = (2..sheet.rowCount).map { r ->
        val key = "$colLetter$r"
        r to (sheet.cells[key]?.value ?: "")
    }
    val sorted = if (ascending) {
        rows.sortedBy { it.second }
    } else {
        rows.sortedByDescending { it.second }
    }

    // Temporary map of row data
    val oldRows = mutableMapOf<Int, Map<String, CellData>>()
    for (r in 2..sheet.rowCount) {
        val rowMap = mutableMapOf<String, CellData>()
        for (c in 0 until sheet.colCount) {
            val key = "${('A'.code + c).toChar()}$r"
            sheet.cells[key]?.let { rowMap[key] = it }
        }
        oldRows[r] = rowMap
    }

    // Write back sorted
    sorted.forEachIndexed { newIdx, pair ->
        val origRowNum = pair.first
        val targetRowNum = newIdx + 2
        val origCells = oldRows[origRowNum] ?: emptyMap()
        for (c in 0 until sheet.colCount) {
            val col = ('A'.code + c).toChar()
            val oldKey = "$col$origRowNum"
            val newKey = "$col$targetRowNum"
            origCells[oldKey]?.let { sheet.cells[newKey] = it }
        }
    }
}

private fun parseSpreadsheetContent(jsonStr: String?): MutableList<SheetModel> {
    if (jsonStr.isNullOrBlank()) return mutableListOf()
    val list = mutableListOf<SheetModel>()
    try {
        val root = JSONObject(jsonStr)
        val arr = root.optJSONArray("sheets") ?: return mutableListOf()
        for (i in 0 until arr.length()) {
            val sObj = arr.getJSONObject(i)
            val name = sObj.optString("name", "Sheet${i + 1}")
            val rowCount = sObj.optInt("rowCount", 25)
            val colCount = sObj.optInt("colCount", 8)
            val cellsObj = sObj.optJSONObject("cells")
            val cellsMap = mutableMapOf<String, CellData>()
            if (cellsObj != null) {
                val keys = cellsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val cObj = cellsObj.getJSONObject(k)
                    val v = cObj.optString("value", "")
                    val b = cObj.optBoolean("bold", false)
                    val it = cObj.optBoolean("italic", false)
                    val fmt = cObj.optString("format", "General")
                    val bg = cObj.optString("bg", "")
                    cellsMap[k] = CellData(value = v, bold = b, italic = it, format = fmt, bg = bg)
                }
            }
            list.add(SheetModel(name, rowCount, colCount, cellsMap))
        }
    } catch (_: Exception) {}
    return list
}

private fun serializeSpreadsheet(sheets: List<SheetModel>, activeIndex: Int): String {
    val root = JSONObject()
    root.put("activeSheetIndex", activeIndex)
    val arr = JSONArray()
    sheets.forEach { s ->
        val sObj = JSONObject()
        sObj.put("name", s.name)
        sObj.put("rowCount", s.rowCount)
        sObj.put("colCount", s.colCount)
        val cellsObj = JSONObject()
        s.cells.forEach { (coord, cell) ->
            val cObj = JSONObject()
            cObj.put("value", cell.value)
            cObj.put("bold", cell.bold)
            cObj.put("italic", cell.italic)
            cObj.put("format", cell.format)
            cObj.put("bg", cell.bg)
            cellsObj.put(coord, cObj)
        }
        sObj.put("cells", cellsObj)
        arr.put(sObj)
    }
    root.put("sheets", arr)
    return root.toString()
}
