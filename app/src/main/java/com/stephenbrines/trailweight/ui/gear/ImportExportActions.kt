package com.stephenbrines.trailweight.ui.gear

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.stephenbrines.trailweight.data.model.GearItem
import com.stephenbrines.trailweight.service.LighterpackRow
import com.stephenbrines.trailweight.service.LighterpackResult
import com.stephenbrines.trailweight.service.LighterpackService
import java.io.File

// Share a CSV of gear items via Android share sheet
fun shareGearCSV(context: Context, items: List<GearItem>, service: LighterpackService) {
    val csv = service.exportGearItems(items)
    val file = File(context.cacheDir, "trailweight-gear.csv")
    file.writeText(csv)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export Gear List"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportCSVSheet(
    onDismiss: () -> Unit,
    onImport: (List<LighterpackRow>) -> Unit,
    service: LighterpackService = LighterpackService(),
) {
    val context = LocalContext.current
    var parsedRows by remember { mutableStateOf<List<LighterpackRow>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val csv = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
            when (val result = service.import_(csv)) {
                is LighterpackResult.Success -> {
                    parsedRows = result.rows
                    errorMessage = null
                    showPreview = true
                }
                is LighterpackResult.Error -> errorMessage = result.message
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to read file"
        }
    }

    if (showPreview && parsedRows.isNotEmpty()) {
        ImportPreviewSheet(
            rows = parsedRows,
            onDismiss = { showPreview = false; onDismiss() },
            onImport = { selectedRows ->
                onImport(selectedRows)
                showPreview = false
                onDismiss()
            }
        )
    } else {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp).padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Import from Lighterpack", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Choose a CSV file exported from Lighterpack.com or any compatible app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                         style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = { filePicker.launch("text/*") },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Choose CSV File") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewSheet(
    rows: List<LighterpackRow>,
    onDismiss: () -> Unit,
    onImport: (List<LighterpackRow>) -> Unit,
) {
    val selected = remember { mutableStateListOf(*Array(rows.size) { true }) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${rows.size} items found", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { selected.replaceAll { false } }) { Text("None") }
                    TextButton(onClick = { selected.replaceAll { true } }) { Text("All") }
                }
            }
            LazyColumn(Modifier.weight(1f, false).heightIn(max = 400.dp)) {
                itemsIndexed(rows) { index, row ->
                    ListItem(
                        headlineContent = { Text(row.name) },
                        supportingContent = {
                            Text("${row.category} · %.0fg".format(row.weightGrams))
                        },
                        leadingContent = {
                            IconButton(onClick = { selected[index] = !selected[index] }) {
                                Icon(
                                    if (selected[index]) Icons.Default.CheckCircle
                                    else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (selected[index]) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
            Button(
                onClick = {
                    val toImport = rows.filterIndexed { i, _ -> selected.getOrElse(i) { false } }
                    onImport(toImport)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = selected.any { it },
            ) {
                val count = selected.count { it }
                Text("Import $count item${if (count == 1) "" else "s"}")
            }
        }
    }
}
