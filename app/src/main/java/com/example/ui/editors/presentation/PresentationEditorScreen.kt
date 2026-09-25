package com.example.ui.editors.presentation

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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.NovaOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class SlideModel(
    var title: String = "Slide Title",
    var subtitle: String = "Subtitle text",
    var layout: String = "TITLE_AND_CONTENT", // "TITLE_SLIDE", "TITLE_AND_CONTENT", "TWO_COLUMNS", "BLANK"
    val bulletPoints: MutableList<String> = mutableListOf(),
    var notes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationEditorScreen(
    file: OfficeFile?,
    onSave: (OfficeFile) -> Unit,
    onBack: () -> Unit,
    onSwitchApp: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var presTitle by remember(file?.id) { mutableStateOf(file?.title ?: "Untitled Presentation") }
    var currentSlideIndex by remember { mutableIntStateOf(0) }
    var themeName by remember { mutableStateOf("Corporate Blue") }

    val slides = remember(file?.id) {
        mutableStateListOf<SlideModel>().apply {
            val loaded = parseSlides(file?.contentJson)
            if (loaded.isNotEmpty()) addAll(loaded)
            else {
                add(SlideModel(title = "Welcome to Presentation", subtitle = "Tap to edit subtitle", layout = "TITLE_SLIDE"))
                add(SlideModel(title = "Agenda & Key Points", subtitle = "", layout = "TITLE_AND_CONTENT", bulletPoints = mutableListOf("Overview", "Strategy", "Execution")))
            }
        }
    }

    var saveState by remember { mutableStateOf(SaveState.SAVED) }
    var showAppSwitcher by remember { mutableStateOf(false) }
    var isPresentingMode by remember { mutableStateOf(false) }
    var showNotesPanel by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }

    fun triggerAutoSave() {
        saveState = SaveState.SAVING
        scope.launch {
            delay(600)
            val json = serializeSlides(presTitle, themeName, slides)
            val updated = (file ?: OfficeFile(title = presTitle, fileType = "PRESENTATION", contentJson = json))
                .copy(title = presTitle, contentJson = json, lastModified = System.currentTimeMillis())
            onSave(updated)
            saveState = SaveState.SAVED
        }
    }

    val currentSlide = slides.getOrNull(currentSlideIndex) ?: slides.first()

    // Presentation Mode (Full Screen Slideshow)
    if (isPresentingMode) {
        PresentationSlideshowOverlay(
            slides = slides,
            initialSlideIndex = currentSlideIndex,
            themeName = themeName,
            onClose = { isPresentingMode = false }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = presTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Slide ${currentSlideIndex + 1} of ${slides.size}",
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
                    Button(
                        onClick = { isPresentingMode = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaOrange),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("start_presentation_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { showAppSwitcher = true },
                        modifier = Modifier.testTag("slide_app_switcher_button")
                    ) {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = "Apps", tint = NovaOrange)
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

                // Quick slide tools: Duplicate, Delete, Change Layout, Speaker Notes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(onClick = { showLayoutDialog = true }) {
                            Icon(Icons.Default.ViewCarousel, contentDescription = "Layout", tint = NovaOrange)
                        }
                        IconButton(
                            onClick = {
                                val copy = currentSlide.copy(bulletPoints = currentSlide.bulletPoints.toMutableList())
                                slides.add(currentSlideIndex + 1, copy)
                                currentSlideIndex++
                                triggerAutoSave()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Slide")
                        }
                        IconButton(
                            onClick = {
                                if (slides.size > 1) {
                                    slides.removeAt(currentSlideIndex)
                                    if (currentSlideIndex >= slides.size) currentSlideIndex = slides.size - 1
                                    triggerAutoSave()
                                }
                            },
                            enabled = slides.size > 1
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Slide")
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showNotesPanel = !showNotesPanel }) {
                            Icon(
                                Icons.Default.Notes,
                                contentDescription = "Speaker Notes",
                                tint = if (showNotesPanel) NovaOrange else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Button(
                            onClick = {
                                val newSlide = SlideModel(
                                    title = "New Slide",
                                    subtitle = "Enter details",
                                    layout = "TITLE_AND_CONTENT",
                                    bulletPoints = mutableListOf("First item", "Second item")
                                )
                                slides.add(newSlide)
                                currentSlideIndex = slides.size - 1
                                triggerAutoSave()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Slide", fontSize = 12.sp)
                        }
                    }
                }

                // Slide Thumbnails Strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    itemsIndexed(slides) { idx, slide ->
                        val isSelected = currentSlideIndex == idx
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = 80.dp, height = 50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) NovaOrange else Color.LightGray,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { currentSlideIndex = idx }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${idx + 1}. ${slide.title}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    color = Color.Black
                                )
                                Text(
                                    text = slide.layout.replace("_", " "),
                                    fontSize = 7.sp,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
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
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .verticalScroll(rememberScrollState())
        ) {
            // Main Slide Canvas (16:9 aspect ratio standard for PowerPoint)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .aspectRatio(16f / 10f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            when (themeName) {
                                "Corporate Blue" -> Color(0xFF0F172A)
                                "Sunset Coral" -> Color(0xFF7C2D12)
                                "Emerald Tech" -> Color(0xFF064E3B)
                                else -> Color(0xFF1E293B)
                            }
                        )
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Title Input
                        BasicTextField(
                            value = currentSlide.title,
                            onValueChange = {
                                currentSlide.title = it
                                triggerAutoSave()
                            },
                            textStyle = TextStyle(
                                fontSize = if (currentSlide.layout == "TITLE_SLIDE") 24.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = if (currentSlide.layout == "TITLE_SLIDE") TextAlign.Center else TextAlign.Start
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("slide_title_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtitle or Content
                        BasicTextField(
                            value = currentSlide.subtitle,
                            onValueChange = {
                                currentSlide.subtitle = it
                                triggerAutoSave()
                            },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = if (currentSlide.layout == "TITLE_SLIDE") TextAlign.Center else TextAlign.Start
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bullet Points for Title & Content layout
                        if (currentSlide.layout != "TITLE_SLIDE") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                currentSlide.bulletPoints.forEachIndexed { bIdx, pt ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("• ", color = NovaOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        BasicTextField(
                                            value = pt,
                                            onValueChange = {
                                                currentSlide.bulletPoints[bIdx] = it
                                                triggerAutoSave()
                                            },
                                            textStyle = TextStyle(fontSize = 13.sp, color = Color.White),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                currentSlide.bulletPoints.removeAt(bIdx)
                                                triggerAutoSave()
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        currentSlide.bulletPoints.add("New point")
                                        triggerAutoSave()
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = NovaOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Add Bullet Point", color = NovaOrange, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Speaker Notes Accordion
            AnimatedVisibility(visible = showNotesPanel) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = NovaOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Speaker Notes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = currentSlide.notes,
                            onValueChange = {
                                currentSlide.notes = it
                                triggerAutoSave()
                            },
                            placeholder = { Text("Private notes only visible in presentation mode...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }
        }
    }

    // Slide Layout Selection Dialog
    if (showLayoutDialog) {
        AlertDialog(
            onDismissRequest = { showLayoutDialog = false },
            title = { Text("Choose Slide Layout") },
            text = {
                val layouts = listOf(
                    "TITLE_SLIDE" to "Title Slide",
                    "TITLE_AND_CONTENT" to "Title + Content",
                    "TWO_COLUMNS" to "Two Columns",
                    "BLANK" to "Blank Slide"
                )
                Column {
                    layouts.forEach { (lKey, lName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentSlide.layout = lKey
                                    triggerAutoSave()
                                    showLayoutDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLayoutDialog = false }) { Text("Cancel") }
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
fun PresentationSlideshowOverlay(
    slides: List<SlideModel>,
    initialSlideIndex: Int,
    themeName: String,
    onClose: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(initialSlideIndex) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var showNotesInOverlay by remember { mutableStateOf(false) }

    // Live Presentation Timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val currentSlide = slides[currentIndex]
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timerStr = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .navigationBarsPadding()
    ) {
        // Main slide projection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
                .background(
                    when (themeName) {
                        "Corporate Blue" -> Color(0xFF0F172A)
                        "Sunset Coral" -> Color(0xFF7C2D12)
                        "Emerald Tech" -> Color(0xFF064E3B)
                        else -> Color(0xFF1E293B)
                    }
                )
                .clickable {
                    // Tap right half to advance, left to go back
                    if (currentIndex < slides.size - 1) currentIndex++
                }
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text(
                    text = currentSlide.title,
                    fontSize = if (currentSlide.layout == "TITLE_SLIDE") 30.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = if (currentSlide.layout == "TITLE_SLIDE") TextAlign.Center else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                if (currentSlide.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentSlide.subtitle,
                        fontSize = 16.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = if (currentSlide.layout == "TITLE_SLIDE") TextAlign.Center else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (currentSlide.bulletPoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    currentSlide.bulletPoints.forEach { pt ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("• ", color = NovaOrange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(pt, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Optional Speaker Notes overlay card
            if (showNotesInOverlay && currentSlide.notes.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD0F172A))
                ) {
                    Text(
                        text = "Notes: ${currentSlide.notes}",
                        color = Color(0xFFFDE047),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Presenter Controls Bar at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF1E293B))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = NovaOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(timerStr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("${currentIndex + 1}/${slides.size}", color = Color(0xFF94A3B8), fontSize = 13.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    enabled = currentIndex > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = Color.White)
                }
                IconButton(
                    onClick = { if (currentIndex < slides.size - 1) currentIndex++ },
                    enabled = currentIndex < slides.size - 1
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
                }
                IconButton(onClick = { showNotesInOverlay = !showNotesInOverlay }) {
                    Icon(Icons.Default.Notes, contentDescription = "Notes", tint = if (showNotesInOverlay) NovaOrange else Color.White)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

private fun parseSlides(jsonStr: String?): List<SlideModel> {
    if (jsonStr.isNullOrBlank()) return emptyList()
    val list = mutableListOf<SlideModel>()
    try {
        val root = JSONObject(jsonStr)
        val arr = root.optJSONArray("slides") ?: return emptyList()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val title = obj.optString("title", "Slide ${i + 1}")
            val sub = obj.optString("subtitle", "")
            val layout = obj.optString("layout", "TITLE_AND_CONTENT")
            val notes = obj.optString("notes", "")
            val bullets = mutableListOf<String>()
            val bArr = obj.optJSONArray("bulletPoints")
            if (bArr != null) {
                for (b in 0 until bArr.length()) {
                    bullets.add(bArr.getString(b))
                }
            }
            list.add(SlideModel(title = title, subtitle = sub, layout = layout, bulletPoints = bullets, notes = notes))
        }
    } catch (_: Exception) {}
    return list
}

private fun serializeSlides(title: String, theme: String, slides: List<SlideModel>): String {
    val root = JSONObject()
    root.put("title", title)
    root.put("theme", theme)
    val arr = JSONArray()
    slides.forEach { s ->
        val obj = JSONObject()
        obj.put("title", s.title)
        obj.put("subtitle", s.subtitle)
        obj.put("layout", s.layout)
        obj.put("notes", s.notes)
        val bArr = JSONArray()
        s.bulletPoints.forEach { bArr.put(it) }
        obj.put("bulletPoints", bArr)
        arr.put(obj)
    }
    root.put("slides", arr)
    return root.toString()
}
