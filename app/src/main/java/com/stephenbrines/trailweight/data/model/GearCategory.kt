package com.stephenbrines.trailweight.data.model

import androidx.compose.ui.graphics.Color

enum class GearCategory(
    val displayName: String,
    val icon: String,
) {
    SHELTER("Shelter", "tent"),
    SLEEP("Sleep System", "bed_double"),
    CLOTHING("Clothing", "checkroom"),
    COOKING("Cooking", "local_fire_department"),
    NAVIGATION("Navigation", "map"),
    FIRST_AID("First Aid", "medical_services"),
    HYGIENE("Hygiene", "shower"),
    FOOD("Food", "restaurant"),
    WATER("Water", "water_drop"),
    ELECTRONICS("Electronics", "bolt"),
    FOOTWEAR("Footwear", "hiking"),
    TOOLS("Tools & Repair", "build"),
    OTHER("Other", "inventory_2");

    val countsTowardBaseWeight: Boolean
        get() = this != FOOD && this != WATER

    companion object {
        fun fromString(value: String): GearCategory =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}
