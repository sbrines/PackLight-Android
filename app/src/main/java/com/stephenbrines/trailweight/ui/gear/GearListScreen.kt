package com.stephenbrines.trailweight.ui.gear

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.stephenbrines.trailweight.data.model.GearItem
import com.stephenbrines.trailweight.service.LighterpackResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GearListScreen(
    navController: NavController,
    padding: PaddingValues,
    viewModel: GearViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddSheet by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<GearItem?>(null) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // CSV file picker for import
    val csvPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val csv = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return@rememberLauncherForActivityResult
        when (val result = viewModel.importFromCsv(csv)) {
            is LighterpackResult.Success -> {
                viewModel.importRows(result.rows)
            }
            is LighterpackResult.Error -> { /* surfaced in sheet */ }
        }
    }

    Scaffold(
        modifier = Modifier.padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Gear Inventory") },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More options")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Import from Lighterpack") },
                                onClick = {
                                    showMenu = false
                                    csvPicker.launch("text/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export gear list") },
                                onClick = {
                                    showMenu = false
                                    shareGearCSV(context, items, com.stephenbrines.trailweight.service.LighterpackService())
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, "Add gear")
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearch,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search gear…") },
                singleLine = true,
            )

            if (items.isEmpty()) {
                GearEmptyState(
                    hasSearch = state.searchQuery.isNotBlank(),
                    onAdd = { showAddSheet = true },
                    onImport = { csvPicker.launch("text/*") },
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { item ->
                        GearItemRow(
                            item = item,
                            onClick = { editItem = item },
                            onDelete = { viewModel.deleteItem(item) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddGearSheet(
            onDismiss = { showAddSheet = false },
            onSave = { item -> viewModel.saveItem(item); showAddSheet = false },
            viewModel = viewModel,
        )
    }
    editItem?.let { item ->
        AddGearSheet(
            existingItem = item,
            onDismiss = { editItem = null },
            onSave = { updated -> viewModel.saveItem(updated); editItem = null },
            viewModel = viewModel,
        )
    }
}

@Composable
private fun GearEmptyState(
    hasSearch: Boolean,
    onAdd: () -> Unit,
    onImport: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (hasSearch) {
                Text("No gear matches your search.",
                     style = MaterialTheme.typography.bodyLarge,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Your gear inventory is empty.",
                     style = MaterialTheme.typography.titleMedium)
                Text("Add items one by one, or import\nan existing Lighterpack list.",
                     style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onImport) { Text("Import CSV") }
                    Button(onClick = onAdd) { Text("Add Gear") }
                }
            }
        }
    }
}

@Composable
private fun GearItemRow(item: GearItem, onClick: () -> Unit, onDelete: () -> Unit) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(item.name) },
        supportingContent = {
            Text(buildString {
                if (item.brand.isNotBlank()) append("${item.brand} · ")
                append(item.category.displayName)
            })
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(item.displayWeight, style = MaterialTheme.typography.bodyMedium)
                if (item.quantityOwned > 1) {
                    Text("×${item.quantityOwned}",
                         style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        modifier = Modifier.clickable { onClick() },
    )

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Delete ${item.name}?") },
            text = { Text("This removes it from your inventory permanently.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showConfirmDelete = false }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}
