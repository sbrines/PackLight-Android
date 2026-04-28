package com.stephenbrines.trailweight.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "weight_snapshots")
data class WeightSnapshot(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val tripName: String,
    val baseWeightGrams: Double,
    val totalWeightGrams: Double,
    val itemCount: Int,
    val recordedAt: Long = System.currentTimeMillis(),
) {
    val classification: String get() = when {
        baseWeightGrams < 2_270 -> "SUL"
        baseWeightGrams < 4_540 -> "UL"
        baseWeightGrams < 9_070 -> "Lightweight"
        else -> "Traditional"
    }
}
