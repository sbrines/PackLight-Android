package com.stephenbrines.trailweight.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "resupply_points",
    foreignKeys = [ForeignKey(
        entity = Trip::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("tripId")],
)
data class ResupplyPoint(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val locationName: String,
    val mileMarker: Double = 0.0,
    val notes: String = "",
    val shippingAddress: String = "",
    val holdForPickup: Boolean = false,
    val estimatedArrivalMs: Long? = null,
    val isSent: Boolean = false,
    val isPickedUp: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val statusLabel: String get() = when {
        isPickedUp -> "Picked Up"
        isSent -> "In Transit"
        else -> "Preparing"
    }
}

@Entity(
    tableName = "resupply_items",
    foreignKeys = [
        ForeignKey(
            entity = ResupplyPoint::class,
            parentColumns = ["id"],
            childColumns = ["resupplyPointId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GearItem::class,
            parentColumns = ["id"],
            childColumns = ["gearItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("resupplyPointId"), Index("gearItemId")],
)
data class ResupplyItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val resupplyPointId: String,
    val gearItemId: String,
    val quantity: Int = 1,
    val notes: String = "",
)
