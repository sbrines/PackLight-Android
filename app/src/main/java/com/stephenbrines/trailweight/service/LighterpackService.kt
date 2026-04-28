package com.stephenbrines.trailweight.service

import com.stephenbrines.trailweight.data.model.GearCategory
import com.stephenbrines.trailweight.data.model.GearItem
import javax.inject.Inject
import javax.inject.Singleton

// Lighterpack CSV format:
// Item Name,Category,desc,qty,weight,unit,url,price,worn,consumable
// Compatible with lighterpack.com import/export

data class LighterpackRow(
    val name: String,
    val category: String,
    val description: String,
    val quantity: Int,
    val weightGrams: Double,
    val url: String,
    val worn: Boolean,
    val consumable: Boolean,
)

sealed class LighterpackResult {
    data class Success(val rows: List<LighterpackRow>) : LighterpackResult()
    data class Error(val message: String) : LighterpackResult()
}

@Singleton
class LighterpackService @Inject constructor() {

    // MARK: Export

    fun exportGearItems(items: List<GearItem>): String {
        val header = "Item Name,Category,desc,qty,weight,unit,url,price,worn,consumable"
        val rows = items.map { item ->
            listOf(
                csv(item.name),
                csv(item.category.displayName),
                csv(item.notes),
                item.quantityOwned.toString(),
                "%.2f".format(item.weightGrams),
                "g",
                csv(item.purchaseUrl),
                "0",
                "0",
                if (item.isConsumable) "1" else "0",
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    // MARK: Import

    fun import_(csv: String): LighterpackResult {
        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return LighterpackResult.Error("File is empty")

        val dataLines = if (lines.first().lowercase().contains("item name"))
            lines.drop(1) else lines

        val rows = mutableListOf<LighterpackRow>()
        for (line in dataLines) {
            if (line.isBlank()) continue
            val fields = parseCSVLine(line)
            if (fields.size < 5) continue

            val rawWeight = fields[4].toDoubleOrNull() ?: 0.0
            val unit = if (fields.size > 5) fields[5].lowercase() else "g"
            val grams = WeightParser.parseToGrams("$rawWeight $unit") ?: rawWeight

            rows.add(LighterpackRow(
                name = fields[0],
                category = if (fields.size > 1) fields[1] else "",
                description = if (fields.size > 2) fields[2] else "",
                quantity = fields[3].toIntOrNull() ?: 1,
                weightGrams = grams,
                url = if (fields.size > 6) fields[6] else "",
                worn = fields.size > 8 && fields[8] == "1",
                consumable = fields.size > 9 && fields[9] == "1",
            ))
        }
        return LighterpackResult.Success(rows)
    }

    fun rowsToGearItems(rows: List<LighterpackRow>): List<GearItem> = rows.map { row ->
        GearItem(
            name = row.name.ifBlank { "Imported Item" },
            category = GearCategory.entries.firstOrNull {
                it.displayName.equals(row.category, ignoreCase = true)
            } ?: GearCategory.OTHER,
            weightGrams = row.weightGrams,
            quantityOwned = maxOf(1, row.quantity),
            isConsumable = row.consumable,
            notes = row.description,
            purchaseUrl = row.url,
        )
    }

    // MARK: Helpers

    private fun csv(s: String): String {
        val escaped = s.replace("\"", "\"\"")
        return if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            "\"$escaped\"" else escaped
    }

    private fun parseCSVLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            when {
                line[i] == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"'); i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                line[i] == ',' && !inQuotes -> {
                    fields.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(line[i])
            }
            i++
        }
        fields.add(current.toString().trim())
        return fields
    }
}
