package com.example.ui.editors.publisher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfficeFile
import com.example.ui.components.AutosaveStatusBadge
import com.example.ui.components.SaveState
import com.example.ui.components.UniversalAppSwitcherSheet
import com.example.ui.theme.NovaPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class CanvasElement(
    val id: Long = System.nanoTime(),
    var type: String = "heading", // "heading", "subheading", "paragraph", "badge", "box", "divider", "button"
    var text: String = "Sample Text",
    var x: Float = 40f,
    var y: Float = 40f,
    var colorHex: String = "#FFFFFF",
    var fontSizeSp: Float = 16f,
    var isLocked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublisherEditorScreen(
    file: OfficeFile?,
    onSave: (OfficeFile) -> Unit,
    onBack: () -> Unit,
    onSwitchApp: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var pubTitle by remember(file?.id) { mutableStateOf(file?.title ?: "Untitled Design") }
    var canvasBgHex by remember { mutableStateOf("#0F172A") }

    val elements = remember(file?.id) {
        mutableStateListOf<CanvasElement>().apply {
            val loaded = parsePublisherElements(file?.contentJson)
            if (loaded.isNotEmpty()) addAll(loaded)
            else {
                add(CanvasElement(type = "badge", text = "GRAND OPENING", x = 90f, y = 30f, colorHex = "#F59E0B", fontSizeSp = 12f))
                add(CanvasElement(type = "heading", text = "NOVA CREATIVE STUDIO", x = 30f, y = 70f, colorHex = "#FFFFFF", fontSizeSp = 22f))
                add(CanvasElement(type = "paragraph", text = "Discover mobile-first desktop publishing. Tap any element to drag, edit text or reorder layers.", x = 30f, y = 140f, colorHex = "#94A3B8", fontSizeSp = 13f))
            }
        }
    }

    var selectedElementId by remember { mutableStateOf<Long?>(elements.firstOrNull()?.id) }
    var saveState by remember { mutableStateOf(SaveState.SAVED) }
    var showAppSwitcher by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    fun triggerAutoSave() {
        saveState = SaveState.SAVING
        scope.launch {
            delay(600)
            val json = serializePublisher(pubTitle, canvasBgHex, elements)
            val updated = (file ?: OfficeFile(title = pubTitle, fileType = "PUBLISHER", contentJson = json))
                .copy(title = pubTitle, contentJson = json, lastModified = System.currentTimeMillis())
            onSave(updated)
            saveState = SaveState.SAVED
        }
    }

    val selectedElement = elements.find { it.id == selectedElementId }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = pubTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${elements.size} layers · Tap to drag",
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
                        onClick = { showTemplateDialog = true },
                        modifier = Modifier.testTag("publisher_templates_button")
                    ) {
                        Icon(imageVector = Icons.Default.FormatShapes, contentDescription = "Templates", tint = NovaPurple)
                    }
                    IconButton(
                        onClick = { showAppSwitcher = true },
                        modifier = Modifier.testTag("publisher_app_switcher_button")
                    ) {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = "Apps", tint = NovaPurple)
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

                // Selected Element Quick Toolbar (Text, Color, Font Size, Duplicate, Layering, Delete)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add New Element
                    IconButton(
                        onClick = {
                            val newElem = CanvasElement(
                                type = "heading",
                                text = "New Text Block",
                                x = 50f,
                                y = 100f,
                                colorHex = "#FFFFFF",
                                fontSizeSp = 18f
                            )
                            elements.add(newElem)
                            selectedElementId = newElem.id
                            triggerAutoSave()
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Element", tint = NovaPurple)
                    }

                    // Bring to front
                    IconButton(
                        onClick = {
                            selectedElement?.let { el ->
                                elements.remove(el)
                                elements.add(el) // move to top of stack
                                triggerAutoSave()
                            }
                        },
                        enabled = selectedElement != null
                    ) {
                        Icon(Icons.Default.FlipToFront, contentDescription = "Bring to Front")
                    }

                    // Send to back
                    IconButton(
                        onClick = {
                            selectedElement?.let { el ->
                                elements.remove(el)
                                elements.add(0, el) // move to bottom of stack
                                triggerAutoSave()
                            }
                        },
                        enabled = selectedElement != null
                    ) {
                        Icon(Icons.Default.FlipToBack, contentDescription = "Send to Back")
                    }

                    // Duplicate
                    IconButton(
                        onClick = {
                            selectedElement?.let { el ->
                                val copy = el.copy(id = System.nanoTime(), x = el.x + 20f, y = el.y + 20f)
                                elements.add(copy)
                                selectedElementId = copy.id
                                triggerAutoSave()
                            }
                        },
                        enabled = selectedElement != null
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate")
                    }

                    // Lock / Unlock
                    IconButton(
                        onClick = {
                            selectedElement?.let { el ->
                                el.isLocked = !el.isLocked
                                triggerAutoSave()
                            }
                        },
                        enabled = selectedElement != null
                    ) {
                        Icon(
                            if (selectedElement?.isLocked == true) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock",
                            tint = if (selectedElement?.isLocked == true) NovaPurple else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Color presets chips
                    val colorPalette = listOf("#FFFFFF", "#F59E0B", "#38BDF8", "#34D399", "#F43F5E", "#A855F7", "#1E293B")
                    colorPalette.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(1.dp, Color.White, CircleShape)
                                .clickable {
                                    selectedElement?.let {
                                        it.colorHex = hex
                                        triggerAutoSave()
                                    }
                                }
                        )
                    }

                    // Delete Element
                    IconButton(
                        onClick = {
                            selectedElement?.let { el ->
                                elements.remove(el)
                                selectedElementId = elements.lastOrNull()?.id
                                triggerAutoSave()
                            }
                        },
                        enabled = selectedElement != null
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Touch Canvas
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .aspectRatio(3f / 4.2f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(canvasBgHex))
                            } catch (_: Exception) { Color(0xFF0F172A) }
                        )
                ) {
                    elements.forEach { elem ->
                        val isSelected = selectedElementId == elem.id
                        val color = try {
                            Color(android.graphics.Color.parseColor(elem.colorHex))
                        } catch (_: Exception) { Color.White }

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(elem.x.roundToInt(), elem.y.roundToInt()) }
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) NovaPurple else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .pointerInput(elem.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (!elem.isLocked) {
                                            elem.x = (elem.x + dragAmount.x).coerceIn(0f, 300f)
                                            elem.y = (elem.y + dragAmount.y).coerceIn(0f, 440f)
                                            selectedElementId = elem.id
                                            triggerAutoSave()
                                        }
                                    }
                                }
                                .clickable { selectedElementId = elem.id }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            when (elem.type) {
                                "badge" -> {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        BasicTextField(
                                            value = elem.text,
                                            onValueChange = {
                                                elem.text = it
                                                triggerAutoSave()
                                            },
                                            textStyle = TextStyle(
                                                fontSize = elem.fontSizeSp.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = color
                                            )
                                        )
                                    }
                                }
                                "button" -> {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(color)
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        BasicTextField(
                                            value = elem.text,
                                            onValueChange = {
                                                elem.text = it
                                                triggerAutoSave()
                                            },
                                            textStyle = TextStyle(
                                                fontSize = elem.fontSizeSp.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                                "divider" -> {
                                    Box(
                                        modifier = Modifier
                                            .width(260.dp)
                                            .height(2.dp)
                                            .background(color)
                                    )
                                }
                                else -> {
                                    BasicTextField(
                                        value = elem.text,
                                        onValueChange = {
                                            elem.text = it
                                            triggerAutoSave()
                                        },
                                        textStyle = TextStyle(
                                            fontSize = elem.fontSizeSp.sp,
                                            fontWeight = if (elem.type == "heading") FontWeight.Bold else FontWeight.Normal,
                                            color = color
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Professional Templates Dialog
    if (showTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = { Text("Choose Publisher Template") },
            text = {
                val templates = listOf(
                    "Event Flyer" to "#0F172A",
                    "Achievement Certificate" to "#1E3A8A",
                    "Modern Business Card" to "#18181B",
                    "Restaurant Menu" to "#451A03",
                    "Conference Poster" to "#312E81"
                )
                Column {
                    templates.forEach { (tmplTitle, bgHex) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    pubTitle = tmplTitle
                                    canvasBgHex = bgHex
                                    elements.clear()
                                    when (tmplTitle) {
                                        "Achievement Certificate" -> {
                                            elements.add(CanvasElement(type = "heading", text = "CERTIFICATE OF EXCELLENCE", x = 20f, y = 50f, colorHex = "#F59E0B", fontSizeSp = 18f))
                                            elements.add(CanvasElement(type = "paragraph", text = "This is proudly presented to honoring exceptional leadership.", x = 20f, y = 110f, colorHex = "#E2E8F0", fontSizeSp = 13f))
                                            elements.add(CanvasElement(type = "badge", text = "VERIFIED HONORS 2026", x = 80f, y = 200f, colorHex = "#34D399", fontSizeSp = 12f))
                                        }
                                        "Modern Business Card" -> {
                                            elements.add(CanvasElement(type = "heading", text = "ALEXANDER CHEN", x = 25f, y = 40f, colorHex = "#FFFFFF", fontSizeSp = 20f))
                                            elements.add(CanvasElement(type = "subheading", text = "Chief Product Architect", x = 25f, y = 75f, colorHex = "#38BDF8", fontSizeSp = 13f))
                                            elements.add(CanvasElement(type = "paragraph", text = "alexander@novaoffice.io • +1 (555) 019-2831", x = 25f, y = 140f, colorHex = "#94A3B8", fontSizeSp = 11f))
                                        }
                                        else -> {
                                            elements.add(CanvasElement(type = "badge", text = "SPECIAL INVITATION", x = 80f, y = 30f, colorHex = "#F59E0B", fontSizeSp = 12f))
                                            elements.add(CanvasElement(type = "heading", text = tmplTitle.uppercase(), x = 30f, y = 80f, colorHex = "#FFFFFF", fontSizeSp = 22f))
                                            elements.add(CanvasElement(type = "paragraph", text = "Experience modern touch-first design on mobile.", x = 30f, y = 140f, colorHex = "#CBD5E1", fontSizeSp = 13f))
                                        }
                                    }
                                    triggerAutoSave()
                                    showTemplateDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(bgHex)))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(tmplTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemplateDialog = false }) { Text("Cancel") }
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

private fun parsePublisherElements(jsonStr: String?): List<CanvasElement> {
    if (jsonStr.isNullOrBlank()) return emptyList()
    val list = mutableListOf<CanvasElement>()
    try {
        val root = JSONObject(jsonStr)
        val arr = root.optJSONArray("elements") ?: return emptyList()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val id = obj.optLong("id", System.nanoTime())
            val type = obj.optString("type", "heading")
            val text = obj.optString("text", "")
            val x = obj.optDouble("x", 40.0).toFloat()
            val y = obj.optDouble("y", 40.0).toFloat()
            val color = obj.optString("color", "#FFFFFF")
            val fontSize = obj.optDouble("fontSize", 16.0).toFloat()
            list.add(CanvasElement(id = id, type = type, text = text, x = x, y = y, colorHex = color, fontSizeSp = fontSize))
        }
    } catch (_: Exception) {}
    return list
}

private fun serializePublisher(title: String, bgHex: String, elements: List<CanvasElement>): String {
    val root = JSONObject()
    root.put("title", title)
    root.put("backgroundColor", bgHex)
    val arr = JSONArray()
    elements.forEach { el ->
        val obj = JSONObject()
        obj.put("id", el.id)
        obj.put("type", el.type)
        obj.put("text", el.text)
        obj.put("x", el.x)
        obj.put("y", el.y)
        obj.put("color", el.colorHex)
        obj.put("fontSize", el.fontSizeSp)
        arr.put(obj)
    }
    root.put("elements", arr)
    return root.toString()
}
