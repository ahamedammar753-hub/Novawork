package com.example.ui.editors.form

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfficeFile
import com.example.ui.components.AutosaveStatusBadge
import com.example.ui.components.SaveState
import com.example.ui.components.UniversalAppSwitcherSheet
import com.example.ui.theme.NovaTeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class FormQuestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    var label: String,
    var type: String, // "TEXT", "NUMBER", "CHECKBOX", "RATING", "DROPDOWN"
    var required: Boolean = false,
    var value: String = "",
    var options: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormEditorScreen(
    file: OfficeFile?,
    onSave: (OfficeFile) -> Unit,
    onBack: () -> Unit,
    onSwitchApp: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var formTitle by remember(file?.id) { mutableStateOf(file?.title ?: "Customer Feedback Survey") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Form, 1: Responses Table

    val questions = remember(file?.id) {
        mutableStateListOf<FormQuestion>().apply {
            val loaded = parseFormQuestions(file?.contentJson)
            if (loaded.isNotEmpty()) addAll(loaded)
            else {
                add(FormQuestion(label = "Full Name", type = "TEXT", required = true, value = "Jane Doe"))
                add(FormQuestion(label = "Overall Satisfaction", type = "RATING", value = "4"))
                add(FormQuestion(label = "Would you recommend NOVA Office to colleagues?", type = "CHECKBOX", value = "true"))
                add(FormQuestion(label = "Primary Department", type = "DROPDOWN", options = listOf("Engineering", "Marketing", "Finance", "Product"), value = "Engineering"))
                add(FormQuestion(label = "Feedback & Feature Requests", type = "TEXT", value = "Great mobile suite! Loving the responsive spreadsheet formulas."))
            }
        }
    }

    // Mock collected responses for table view
    val responses = remember {
        mutableStateListOf(
            listOf("Jane Doe", "5 Stars", "Yes", "Engineering", "Loving the responsive spreadsheet"),
            listOf("Marcus Vance", "4 Stars", "Yes", "Marketing", "Very smooth presentation controls"),
            listOf("Elena Rostova", "5 Stars", "Yes", "Finance", "Form formulas work great")
        )
    }

    var saveState by remember { mutableStateOf(SaveState.SAVED) }
    var showAppSwitcher by remember { mutableStateOf(false) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    fun triggerAutoSave() {
        saveState = SaveState.SAVING
        scope.launch {
            delay(600)
            val json = serializeForm(formTitle, questions)
            val updated = (file ?: OfficeFile(title = formTitle, fileType = "FORM", contentJson = json))
                .copy(title = formTitle, contentJson = json, lastModified = System.currentTimeMillis())
            onSave(updated)
            saveState = SaveState.SAVED
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = formTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${questions.size} questions · ${responses.size} responses",
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
                        onClick = {
                            val csv = StringBuilder()
                            csv.append(questions.joinToString(",") { "\"${it.label}\"" }).append("\n")
                            responses.forEach { r ->
                                csv.append(r.joinToString(",") { "\"$it\"" }).append("\n")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TITLE, "$formTitle-responses.csv")
                                putExtra(Intent.EXTRA_TEXT, csv.toString())
                                type = "text/csv"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Form Responses CSV"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export CSV", tint = NovaTeal)
                    }
                    IconButton(
                        onClick = { showAppSwitcher = true },
                        modifier = Modifier.testTag("form_app_switcher_button")
                    ) {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = "Apps", tint = NovaTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (selectedTab == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { showAddQuestionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_question_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Question Field")
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Form Preview & Fill", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Description, contentDescription = null, tint = NovaTeal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Responses (${responses.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = NovaTeal) }
                )
            }

            if (selectedTab == 0) {
                // Form questions list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    itemsIndexed(questions) { idx, q ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${q.label}${if (q.required) " *" else ""}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            questions.removeAt(idx)
                                            triggerAutoSave()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                when (q.type) {
                                    "CHECKBOX" -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = q.value.toBoolean(),
                                                onCheckedChange = {
                                                    q.value = it.toString()
                                                    triggerAutoSave()
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Yes / Agreed", fontSize = 13.sp)
                                        }
                                    }
                                    "RATING" -> {
                                        val currentRating = q.value.toIntOrNull() ?: 3
                                        Row {
                                            (1..5).forEach { star ->
                                                IconButton(
                                                    onClick = {
                                                        q.value = star.toString()
                                                        triggerAutoSave()
                                                    }
                                                ) {
                                                    Icon(
                                                        if (star <= currentRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                        contentDescription = "$star Stars",
                                                        tint = if (star <= currentRating) Color(0xFFF59E0B) else Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    "DROPDOWN" -> {
                                        var expanded by remember { mutableStateOf(false) }
                                        Box {
                                            OutlinedTextField(
                                                value = q.value,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Select option") },
                                                modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                                            )
                                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                                q.options.forEach { opt ->
                                                    DropdownMenuItem(
                                                        text = { Text(opt) },
                                                        onClick = {
                                                            q.value = opt
                                                            expanded = false
                                                            triggerAutoSave()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        OutlinedTextField(
                                            value = q.value,
                                            onValueChange = {
                                                q.value = it
                                                triggerAutoSave()
                                            },
                                            placeholder = { Text("Enter answer...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = q.type == "NUMBER"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                // Collected responses structured table
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Survey Responses Log", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            responses.forEachIndexed { rIdx, row ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Submission #${rIdx + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NovaTeal)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        row.forEachIndexed { qIdx, answer ->
                                            val qLabel = questions.getOrNull(qIdx)?.label ?: "Question ${qIdx + 1}"
                                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text("$qLabel: ", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFF475569))
                                                Text(answer, fontSize = 11.sp, color = Color(0xFF0F172A))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Question Dialog
    if (showAddQuestionDialog) {
        var labelInput by remember { mutableStateOf("") }
        var typeInput by remember { mutableStateOf("TEXT") }

        AlertDialog(
            onDismissRequest = { showAddQuestionDialog = false },
            title = { Text("Add Form Field") },
            text = {
                Column {
                    OutlinedTextField(
                        value = labelInput,
                        onValueChange = { labelInput = it },
                        label = { Text("Question Label") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Field Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    val types = listOf("TEXT" to "Text", "NUMBER" to "Number", "CHECKBOX" to "Checkbox", "RATING" to "Star Rating")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        types.forEach { (tKey, tLabel) ->
                            val active = typeInput == tKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) NovaTeal else Color(0xFFE2E8F0))
                                    .clickable { typeInput = tKey }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (labelInput.isNotBlank()) {
                        questions.add(FormQuestion(label = labelInput, type = typeInput))
                        triggerAutoSave()
                    }
                    showAddQuestionDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddQuestionDialog = false }) { Text("Cancel") }
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

private fun parseFormQuestions(jsonStr: String?): List<FormQuestion> {
    if (jsonStr.isNullOrBlank()) return emptyList()
    val list = mutableListOf<FormQuestion>()
    try {
        val root = JSONObject(jsonStr)
        val arr = root.optJSONArray("questions") ?: return emptyList()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val label = obj.optString("label", "")
            val type = obj.optString("type", "TEXT")
            val req = obj.optBoolean("required", false)
            val v = obj.optString("value", "")
            val opts = mutableListOf<String>()
            val optArr = obj.optJSONArray("options")
            if (optArr != null) {
                for (j in 0 until optArr.length()) opts.add(optArr.getString(j))
            }
            list.add(FormQuestion(label = label, type = type, required = req, value = v, options = opts))
        }
    } catch (_: Exception) {}
    return list
}

private fun serializeForm(title: String, questions: List<FormQuestion>): String {
    val root = JSONObject()
    root.put("title", title)
    val arr = JSONArray()
    questions.forEach { q ->
        val obj = JSONObject()
        obj.put("label", q.label)
        obj.put("type", q.type)
        obj.put("required", q.required)
        obj.put("value", q.value)
        val optArr = JSONArray()
        q.options.forEach { optArr.put(it) }
        obj.put("options", optArr)
        arr.put(obj)
    }
    root.put("questions", arr)
    return root.toString()
}
