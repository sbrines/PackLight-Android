package com.stephenbrines.packlight.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stephenbrines.packlight.data.model.GearItem
import com.stephenbrines.packlight.data.model.PackListItem
import com.stephenbrines.packlight.data.model.PackListItemWithGear
import com.stephenbrines.packlight.data.model.Trip
import com.stephenbrines.packlight.data.repository.GearRepository
import com.stephenbrines.packlight.data.repository.TripRepository
import com.stephenbrines.packlight.service.WeightCalculator
import com.stephenbrines.packlight.service.WeightSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class WeightUnit(val label: String) {
    GRAMS("g"), OUNCES("oz"), KILOGRAMS("kg"), POUNDS("lb")
}

data class WeightUiState(
    val trips: List<Trip> = emptyList(),
    val selectedTrip: Trip? = null,
    val summary: WeightSummary = WeightSummary.EMPTY,
    val displayUnit: WeightUnit = WeightUnit.OUNCES,
)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val tripRepo: TripRepository,
    private val gearRepo: GearRepository,
) : ViewModel() {

    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    private val _displayUnit = MutableStateFlow(WeightUnit.OUNCES)

    val trips: StateFlow<List<Trip>> = tripRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val summary: StateFlow<WeightSummary> = _selectedTrip.flatMapLatest { trip ->
        if (trip == null) return@flatMapLatest flowOf(WeightSummary.EMPTY)
        tripRepo.getPackLists(trip.id).flatMapLatest { packLists ->
            val packList = packLists.firstOrNull() ?: return@flatMapLatest flowOf(WeightSummary.EMPTY)
            combine(
                tripRepo.getPackListItems(packList.id),
                gearRepo.getAll(),
            ) { items, allGear ->
                val gearMap = allGear.associateBy { it.id }
                val withGear = items.map { item ->
                    PackListItemWithGear(item, gearMap[item.gearItemId])
                }
                WeightCalculator.calculate(withGear)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, WeightSummary.EMPTY)

    val displayUnit: StateFlow<WeightUnit> = _displayUnit.asStateFlow()
    val selectedTrip: StateFlow<Trip?> = _selectedTrip.asStateFlow()

    fun selectTrip(trip: Trip?) { _selectedTrip.value = trip }
    fun setUnit(unit: WeightUnit) { _displayUnit.value = unit }

    fun format(grams: Double): String = when (_displayUnit.value) {
        WeightUnit.GRAMS -> "%.0f g".format(grams)
        WeightUnit.OUNCES -> "%.1f oz".format(grams / 28.3495)
        WeightUnit.KILOGRAMS -> "%.3f kg".format(grams / 1000)
        WeightUnit.POUNDS -> "%.2f lb".format(grams / 453.592)
    }
}
