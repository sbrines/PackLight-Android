package com.stephenbrines.packlight.ui.gear

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stephenbrines.packlight.data.model.GearCategory
import com.stephenbrines.packlight.data.model.GearItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGearSheet(
    existingItem: GearItem? = null,
    onDismiss: () -> Unit,
    onSave: (GearItem) -> Unit,
    viewModel: GearViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var brand by remember { mutableStateOf(existingItem?.brand ?: "") }
    var category by remember { mutableStateOf(existingItem?.category ?: GearCategory.OTHER) }
    var weightStr by remember { mutableStateOf(existingItem?.weightGrams?.let { "%.0f".format(it) } ?: "") }
    var quantity by remember { mutableIntStateOf(existingItem?.quantityOwned ?: 1) }
    var isConsumable by remember { mutableStateOf(existingItem?.isConsumable ?: false) }
    var notes by remember { mutableStateOf(existingItem?.notes ?: "") }
    var urlStr by remember { mutableStateOf(existingItem?.purchaseUrl ?: "") }

    // Apply fetched metadata
    LaunchedEffect(state.fetchedMetadata) {
        state.fetchedMetadata?.let { meta ->
            if (name.isBlank()) name = meta.name
            if (weightStr.isBlank() && meta.weightGrams != null) {
                weightStr = "%.0f".format(meta.weightGrams)
            }
            viewModel.clearFetchedMetadata()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (existingItem != null) "Edit Item" else "Add Gear",
                style = MaterialTheme.typography.titleLarge,
            )

            // URL import
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = urlStr,
                    onValueChange = { urlStr = it },
                    label = { Text("Product URL") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                Spacer(Modifier.width(8.dp))
                if (state.isFetchingUrl) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = { if (urlStr.isNotBlank()) viewModel.fetchFromUrl(urlStr) },
                        enabled = urlStr.isNotBlank(),
                    ) {
                        Icon(Icons.Default.Download, "Fetch from URL")
                    }
                }
            }
            state.urlFetchError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Category dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = category.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    GearCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = { category = cat; expanded = false },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Weight (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = quantity.toString(),
                    onValueChange = { quantity = it.toIntOrNull()?.coerceAtLeast(1) ?: quantity },
                    label = { Text("Qty") },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isConsumable, onCheckedChange = { isConsumable = it })
                Text("Consumable (food / fuel)", Modifier.padding(start = 4.dp))
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )

            Button(
                onClick = {
                    val item = (existingItem ?: GearItem(name = name)).copy(
                        name = name,
                        brand = brand,
                        category = category,
                        weightGrams = weightStr.toDoubleOrNull() ?: 0.0,
                        quantityOwned = quantity,
                        isConsumable = isConsumable,
                        notes = notes,
                        purchaseUrl = urlStr,
                        updatedAt = System.currentTimeMillis(),
                    )
                    onSave(item)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
            ) {
                Text(if (existingItem != null) "Save Changes" else "Add to Inventory")
            }
        }
    }
}
