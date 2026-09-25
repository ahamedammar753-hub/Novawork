package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileType
import com.example.ui.theme.NovaCobalt
import com.example.ui.theme.NovaCrimson
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaOrange
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaTeal
import com.example.ui.theme.Slate500

enum class SaveState {
    SAVED, SAVING, FAILED
}

@Composable
fun AutosaveStatusBadge(
    saveState: SaveState,
    onManualSave: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                when (saveState) {
                    SaveState.SAVED -> Color(0xFFE6F4EA)
                    SaveState.SAVING -> Color(0xFFFEF3C7)
                    SaveState.FAILED -> Color(0xFFFCE8E6)
                }
            )
            .clickable { onManualSave() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (saveState) {
            SaveState.SAVED -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Saved",
                    tint = Color(0xFF137333),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "🟢 Saved",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF137333)
                )
            }
            SaveState.SAVING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFFB45309)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "🟡 Saving...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB45309)
                )
            }
            SaveState.FAILED -> {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Save failed",
                    tint = Color(0xFFC5221F),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "🔴 Save failed",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC5221F)
                )
            }
        }
    }
}

data class AppSwitcherItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val fileType: FileType? = null,
    val actionType: String // "DOC", "SHEET", "SLIDE", "PUB", "PDF", "FORM", "FILES", "HOME", "SETTINGS"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalAppSwitcherSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSelectApp: (actionType: String) -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val apps = listOf(
        AppSwitcherItem("Documents", "Word-style editor", Icons.Default.Description, NovaCobalt, FileType.DOCUMENT, "DOC"),
        AppSwitcherItem("Spreadsheets", "Excel-grade sheets", Icons.Default.GridOn, NovaEmerald, FileType.SPREADSHEET, "SHEET"),
        AppSwitcherItem("Presentations", "Slide deck maker", Icons.Default.Slideshow, NovaOrange, FileType.PRESENTATION, "SLIDE"),
        AppSwitcherItem("Publisher", "Page & flyer designer", Icons.Default.ViewQuilt, NovaPurple, FileType.PUBLISHER, "PUB"),
        AppSwitcherItem("PDF Tools", "Read, merge & split", Icons.Default.PictureAsPdf, NovaCrimson, FileType.PDF, "PDF"),
        AppSwitcherItem("Forms", "Form & survey builder", Icons.Default.Feed, NovaTeal, FileType.FORM, "FORM"),
        AppSwitcherItem("Files", "Manager & storage", Icons.Default.Folder, Color(0xFF475569), null, "FILES"),
        AppSwitcherItem("Home Hub", "Main Office dashboard", Icons.Default.Home, Color(0xFF2563EB), null, "HOME"),
        AppSwitcherItem("Settings", "App security & theme", Icons.Default.Settings, Color(0xFF64748B), null, "SETTINGS")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("app_switcher_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NovaCobalt.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Apps Switcher",
                        tint = NovaCobalt,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Universal App Switcher",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current work auto-saved before switching",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(apps) { app ->
                    Card(
                        modifier = Modifier
                            .padding(6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onSelectApp(app.actionType)
                                onDismiss()
                            }
                            .testTag("switcher_${app.actionType.lowercase()}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(app.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = app.icon,
                                    contentDescription = app.title,
                                    tint = app.color,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = app.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
