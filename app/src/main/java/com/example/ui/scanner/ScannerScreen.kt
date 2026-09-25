package com.example.ui.scanner

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCobalt
import com.example.ui.theme.NovaCrimson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onSaveScanAsPdf: (title: String, scanText: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCaptured by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Magic Color") } // "Original", "Black & White", "Grayscale", "Magic Color"
    var showCropBounds by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!hasCaptured) {
                    // Shutter Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .background(NovaCobalt)
                            .clickable { hasCaptured = true }
                            .testTag("scanner_shutter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Align document inside viewfinder and tap capture", color = Color(0xFF94A3B8), fontSize = 12.sp)
                } else {
                    // Filter selection row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val filters = listOf("Original", "B&W", "Grayscale", "Magic Color")
                        filters.forEach { f ->
                            val active = selectedFilter == f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) NovaCobalt else Color(0xFF334155))
                                    .clickable { selectedFilter = f }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    f,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { hasCaptured = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Retake")
                        }

                        Button(
                            onClick = {
                                onSaveScanAsPdf(
                                    "Scanned Document ${System.currentTimeMillis() % 10000}",
                                    "Processed scan image with filter '$selectedFilter'. Text recognized via mobile optical detection."
                                )
                                Toast.makeText(context, "Saved scanned document to PDF", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaCrimson),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save as PDF")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Viewfinder / Captured document sheet
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(3f / 4.2f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (selectedFilter) {
                        "B&W" -> Color(0xFFE2E8F0)
                        "Grayscale" -> Color(0xFFCBD5E1)
                        "Magic Color" -> Color(0xFFFEF9C3)
                        else -> Color.White
                    }
                )
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column {
                        Text(
                            text = if (hasCaptured) "NOVA SCANNER PROCESSED" else "ALIGN DOCUMENT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (hasCaptured) NovaCobalt else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(Color(0xFFCBD5E1)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.8f).height(10.dp).background(Color(0xFFE2E8F0)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.9f).height(10.dp).background(Color(0xFFE2E8F0)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.6f).height(10.dp).background(Color(0xFFE2E8F0)))
                    }

                    // Auto Edge Detection Overlay
                    if (!hasCaptured || showCropBounds) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            // Draw corner guidelines
                            val stroke = 4f
                            val cornerLen = 30f
                            // Top Left
                            drawLine(NovaCobalt, Offset(0f, 0f), Offset(cornerLen, 0f), stroke)
                            drawLine(NovaCobalt, Offset(0f, 0f), Offset(0f, cornerLen), stroke)
                            // Top Right
                            drawLine(NovaCobalt, Offset(w, 0f), Offset(w - cornerLen, 0f), stroke)
                            drawLine(NovaCobalt, Offset(w, 0f), Offset(w, cornerLen), stroke)
                            // Bottom Left
                            drawLine(NovaCobalt, Offset(0f, h), Offset(cornerLen, h), stroke)
                            drawLine(NovaCobalt, Offset(0f, h), Offset(0f, h - cornerLen), stroke)
                            // Bottom Right
                            drawLine(NovaCobalt, Offset(w, h), Offset(w - cornerLen, h), stroke)
                            drawLine(NovaCobalt, Offset(w, h), Offset(w, h - cornerLen), stroke)
                        }
                    }
                }
            }
        }
    }
}
