package com.stephenbrines.packlight.ui.weight

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stephenbrines.packlight.data.model.Trip
import com.stephenbrines.packlight.data.model.WeightSnapshot
import com.stephenbrines.packlight.service.CategoryWeight
import com.stephenbrines.packlight.service.WeightSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightDashboardScreen(
    padding: PaddingValues,
    viewModel: WeightViewModel = hiltViewModel(),
) {
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    val selectedTrip by viewModel.selectedTrip.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val displayUnit by viewModel.displayUnit.collectAsStateWithLifecycle()
    val snapshots by viewModel.snapshots.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Weight") },
                actions = {
                    if (selectedTrip != null && summary.totalWeightGrams > 0) {
                        IconButton(onClick = {
                            viewModel.saveSnapshot(selectedTrip?.name ?: "Trip")
                        }) {
                            Icon(Icons.Default.Save, "Save weight snapshot")
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TripSelector(trips = trips, selected = selectedTrip, onSelect = viewModel::selectTrip)
            }

            // Weight history
            if (snapshots.isNotEmpty()) {
                item {
                    Text("Weight History", style = MaterialTheme.typography.titleMedium,
                         modifier = Modifier.padding(top = 8.dp))
                }
                items(snapshots.reversed().take(5), key = { it.id }) { snap ->
                    WeightHistoryRow(snap = snap, format = viewModel::format,
                                    onDelete = { viewModel.deleteSnapshot(snap) })
                }
            }

            if (selectedTrip != null) {
                item {
                    WeightSummaryCard(summary = summary, format = viewModel::format)
                }
                item {
                    ClassificationCard(summary = summary)
                }
                item {
                    UnitSelector(selected = displayUnit, onSelect = viewModel::setUnit)
                }
                items(summary.byCategory, key = { it.category.name }) { cat ->
                    CategoryWeightRow(cat = cat, format = viewModel::format)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripSelector(trips: List<Trip>, selected: Trip?, onSelect: (Trip?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: "Select a trip",
            onValueChange = {},
            readOnly = true,
            label = { Text("Trip") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("None") }, onClick = { onSelect(null); expanded = false })
            trips.forEach { trip ->
                DropdownMenuItem(
                    text = { Text(trip.name) },
                    onClick = { onSelect(trip); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun WeightSummaryCard(summary: WeightSummary, format: (Double) -> String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Weight Breakdown", style = MaterialTheme.typography.titleMedium)
            WeightRow("Base Weight", summary.baseWeightGrams, format, bold = true)
            WeightRow("Worn Weight", summary.wornWeightGrams, format)
            WeightRow("Consumables", summary.consumableWeightGrams, format)
            HorizontalDivider()
            WeightRow("Total Pack Weight", summary.totalWeightGrams, format, bold = true)
        }
    }
}

@Composable
private fun WeightRow(label: String, grams: Double, format: (Double) -> String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) androidx.compose.ui.text.font.FontWeight.SemiBold else null)
        Text(format(grams), style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace))
    }
}

@Composable
private fun ClassificationCard(summary: WeightSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(summary.classification, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun WeightHistoryRow(
    snap: WeightSnapshot,
    format: (Double) -> String,
    onDelete: () -> Unit,
) {
    var showDelete by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(snap.tripName) },
        supportingContent = {
            val date = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)
                .format(java.util.Date(snap.recordedAt))
            Text("$date · ${snap.classification}")
        },
        trailingContent = {
            Text(format(snap.baseWeightGrams),
                 style = MaterialTheme.typography.bodyMedium.copy(
                     fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace))
        },
        modifier = Modifier.clickable { showDelete = !showDelete },
    )
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete snapshot?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDelete = false }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun UnitSelector(selected: WeightUnit, onSelect: (WeightUnit) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        WeightUnit.entries.forEachIndexed { index, unit ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index, WeightUnit.entries.size),
                onClick = { onSelect(unit) },
                selected = unit == selected,
                label = { Text(unit.label) },
            )
        }
    }
}

@Composable
private fun CategoryWeightRow(cat: CategoryWeight, format: (Double) -> String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(cat.category.displayName, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { (cat.percentage / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(format(cat.weightGrams), style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace))
            Text("%.0f%%".format(cat.percentage),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
