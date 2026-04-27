package com.stephenbrines.packlight.ui.gear

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.stephenbrines.packlight.data.model.GearItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GearListScreen(
    navController: NavController,
    padding: PaddingValues,
    viewModel: GearViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by viewModel.filteredItems.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<GearItem?>(null) }

    Scaffold(
        modifier = Modifier.padding(padding),
        topBar = {
            TopAppBar(
                title = { Text("Gear Inventory") },
                actions = {
                    IconButton(onClick = { /* toggle search */ }) {
                        Icon(Icons.Default.Search, "Search")
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
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search gear…") },
                singleLine = true,
            )

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No gear yet. Tap + to add items.", style = MaterialTheme.typography.bodyLarge)
                }
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
            onSave = { item ->
                viewModel.saveItem(item)
                showAddSheet = false
            },
            viewModel = viewModel,
        )
    }
    editItem?.let { item ->
        AddGearSheet(
            existingItem = item,
            onDismiss = { editItem = null },
            onSave = { updated ->
                viewModel.saveItem(updated)
                editItem = null
            },
            viewModel = viewModel,
        )
    }
}

@Composable
private fun GearItemRow(item: GearItem, onClick: () -> Unit, onDelete: () -> Unit) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(item.name) },
        supportingContent = {
            Text(
                buildString {
                    if (item.brand.isNotBlank()) append("${item.brand} · ")
                    append(item.category.displayName)
                }
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(item.displayWeight, style = MaterialTheme.typography.bodyMedium)
                if (item.quantityOwned > 1) {
                    Text("×${item.quantityOwned}", style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        modifier = androidx.compose.ui.Modifier.padding(0.dp)
            .let { mod -> mod },
    )

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Delete ${item.name}?") },
            text = { Text("This will remove it from your inventory.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showConfirmDelete = false }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}
