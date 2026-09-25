package com.example.ui.drawing

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCobalt

data class DrawLine(
    val start: Offset,
    val end: Offset,
    val color: Color,
    val strokeWidth: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    onSaveDrawing: (strokeCount: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lines = remember { mutableStateListOf<DrawLine>() }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }
    var isEraser by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drawing & Signature Pad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { lines.clear() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Canvas")
                    }
                    Button(
                        onClick = {
                            onSaveDrawing(lines.size)
                            Toast.makeText(context, "Drawing saved to gallery", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCobalt),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontSize = 12.sp)
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

                // Tools row (Pen, Highlighter, Eraser, Widths, Colors)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tool types
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                isEraser = false
                                strokeWidth = 6f
                            }
                        ) {
                            Icon(Icons.Default.Create, contentDescription = "Pen", tint = if (!isEraser && strokeWidth == 6f) NovaCobalt else MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(
                            onClick = {
                                isEraser = false
                                strokeWidth = 18f
                            }
                        ) {
                            Icon(Icons.Default.Brush, contentDescription = "Highlighter", tint = if (!isEraser && strokeWidth == 18f) NovaCobalt else MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(
                            onClick = { isEraser = true }
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = "Eraser", tint = if (isEraser) NovaCobalt else MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Color palette
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val colors = listOf(Color.Black, Color(0xFF2563EB), Color(0xFF059669), Color(0xFFEA580C), Color(0xFFDC2626))
                        colors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (selectedColor == c && !isEraser) 2.dp else 0.dp,
                                        color = if (selectedColor == c && !isEraser) Color.Gray else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColor = c
                                        isEraser = false
                                    }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val line = DrawLine(
                            start = change.position - dragAmount,
                            end = change.position,
                            color = if (isEraser) Color.White else selectedColor,
                            strokeWidth = if (isEraser) 30f else strokeWidth
                        )
                        lines.add(line)
                    }
                }
        ) {
            lines.forEach { line ->
                drawLine(
                    color = line.color,
                    start = line.start,
                    end = line.end,
                    strokeWidth = line.strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
