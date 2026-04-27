package com.stephenbrines.packlight.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "pack_lists",
    foreignKeys = [ForeignKey(
        entity = Trip::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("tripId")],
)
data class PackList(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val name: String = "My Pack List",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "pack_list_items",
    foreignKeys = [
        ForeignKey(
            entity = PackList::class,
            parentColumns = ["id"],
            childColumns = ["packListId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GearItem::class,
            parentColumns = ["id"],
            childColumns = ["gearItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("packListId"), Index("gearItemId")],
)
data class PackListItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val packListId: String,
    val gearItemId: String,
    val packedQuantity: Int = 1,
    val isWorn: Boolean = false,
    val isConsumed: Boolean = false,
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis(),
)

data class PackListWithItems(
    val packList: PackList,
    val items: List<PackListItemWithGear>,
) {
    val baseWeightGrams: Double get() = items
        .filter { !it.item.isWorn && !(it.gear?.isConsumable ?: false) }
        .sumOf { (it.gear?.weightGrams ?: 0.0) * it.item.packedQuantity }

    val wornWeightGrams: Double get() = items
        .filter { it.item.isWorn }
        .sumOf { (it.gear?.weightGrams ?: 0.0) * it.item.packedQuantity }

    val consumableWeightGrams: Double get() = items
        .filter { it.gear?.isConsumable == true }
        .sumOf { (it.gear?.weightGrams ?: 0.0) * it.item.packedQuantity }

    val totalWeightGrams: Double get() = baseWeightGrams + wornWeightGrams + consumableWeightGrams
    val packWeightGrams: Double get() = baseWeightGrams + consumableWeightGrams
}

data class PackListItemWithGear(
    val item: PackListItem,
    val gear: GearItem?,
) {
    val totalWeightGrams: Double get() = (gear?.weightGrams ?: 0.0) * item.packedQuantity
}
