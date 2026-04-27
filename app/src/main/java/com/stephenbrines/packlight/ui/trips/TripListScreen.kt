package com.stephenbrines.packlight.ui.trips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.stephenbrines.packlight.data.model.Trip
import com.stephenbrines.packlight.data.model.TerrainType
import com.stephenbrines.packlight.data.model.TripStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    navController: NavController,
    padding: PaddingValues,
    viewModel: TripViewModel = hiltViewModel(),
) {
    val trips by viewModel.filteredTrips.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    Scaffold(
        modifier = Modifier.padding(padding),
        topBar = { TopAppBar(title = { Text("Trips") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, "Add trip")
            }
        }
    ) { innerPadding ->
        LazyColumn(Modifier.padding(innerPadding)) {
            items(trips, key = { it.id }) { trip ->
                ListItem(
                    headlineContent = { Text(trip.name) },
                    supportingContent = {
                        Text("${trip.formattedDateRange} · ${trip.terrain.displayName}")
                    },
                    trailingContent = {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(trip.status.displayName, style = MaterialTheme.typography.labelSmall) },
                            )
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    },
                    modifier = Modifier.padding(0.dp)
                )
                HorizontalDivider()
            }
        }
    }

    if (showAddSheet) {
        AddTripSheet(
            onDismiss = { showAddSheet = false },
            onSave = { trip ->
                viewModel.createTrip(trip)
                showAddSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTripSheet(onDismiss: () -> Unit, onSave: (Trip) -> Unit) {
    var name by remember { mutableStateOf("") }
    var trailName by remember { mutableStateOf("") }
    var terrain by remember { mutableStateOf(TerrainType.MIXED) }
    var distanceStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("New Trip", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Trip Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(value = trailName, onValueChange = { trailName = it },
                label = { Text("Trail Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(value = distanceStr, onValueChange = { distanceStr = it },
                label = { Text("Distance (miles)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))

            // Terrain picker
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(value = terrain.displayName, onValueChange = {}, readOnly = true,
                    label = { Text("Terrain") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    TerrainType.entries.forEach { t ->
                        DropdownMenuItem(text = { Text(t.displayName) },
                            onClick = { terrain = t; expanded = false })
                    }
                }
            }

            OutlinedTextField(value = notes, onValueChange = { notes = it },
                label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)

            Button(
                onClick = {
                    onSave(Trip(name = name, trailName = trailName, terrain = terrain,
                        distanceMiles = distanceStr.toDoubleOrNull() ?: 0.0, notes = notes))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
            ) { Text("Create Trip") }
        }
    }
}
