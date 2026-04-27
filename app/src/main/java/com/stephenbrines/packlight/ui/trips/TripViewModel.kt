package com.stephenbrines.packlight.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stephenbrines.packlight.data.model.*
import com.stephenbrines.packlight.data.repository.TripRepository
import com.stephenbrines.packlight.service.GearRecommendationEngine
import com.stephenbrines.packlight.service.GearRecommendation
import com.stephenbrines.packlight.service.TripConditions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripUiState(
    val trips: List<Trip> = emptyList(),
    val searchQuery: String = "",
    val filterStatus: TripStatus? = null,
)

@HiltViewModel
class TripViewModel @Inject constructor(
    private val repo: TripRepository,
    private val recommendationEngine: GearRecommendationEngine,
) : ViewModel() {

    private val _state = MutableStateFlow(TripUiState())
    val state: StateFlow<TripUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().collect { trips ->
                _state.update { it.copy(trips = trips) }
            }
        }
    }

    val filteredTrips: StateFlow<List<Trip>> = _state.map { s ->
        var result = s.trips
        s.filterStatus?.let { status -> result = result.filter { it.status == status } }
        if (s.searchQuery.isNotBlank()) {
            result = result.filter {
                it.name.contains(s.searchQuery, ignoreCase = true) ||
                it.trailName.contains(s.searchQuery, ignoreCase = true)
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearch(q: String) = _state.update { it.copy(searchQuery = q) }
    fun setFilter(status: TripStatus?) = _state.update { it.copy(filterStatus = status) }

    fun createTrip(trip: Trip) = viewModelScope.launch(Dispatchers.IO) {
        repo.createTrip(trip)
    }

    fun updateTrip(trip: Trip) = viewModelScope.launch(Dispatchers.IO) { repo.update(trip) }
    fun deleteTrip(trip: Trip) = viewModelScope.launch(Dispatchers.IO) { repo.delete(trip) }

    fun getPackLists(tripId: String) = repo.getPackLists(tripId)
    fun getPackListItems(packListId: String) = repo.getPackListItems(packListId)
    fun getGearForPackList(packListId: String) = repo.getGearForPackList(packListId)

    fun addGearToPackList(packListId: String, gearItemId: String,
                          quantity: Int = 1, isWorn: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            repo.addGearToPackList(packListId, gearItemId, quantity, isWorn)
        }

    fun updatePackListItem(item: PackListItem) = viewModelScope.launch(Dispatchers.IO) {
        repo.updatePackListItem(item)
    }

    fun removePackListItem(item: PackListItem) = viewModelScope.launch(Dispatchers.IO) {
        repo.removePackListItem(item)
    }

    fun getResupplyPoints(tripId: String) = repo.getResupplyPoints(tripId)

    fun addResupplyPoint(tripId: String, locationName: String, mileMarker: Double) =
        viewModelScope.launch(Dispatchers.IO) {
            repo.insertResupplyPoint(
                ResupplyPoint(tripId = tripId, locationName = locationName, mileMarker = mileMarker)
            )
        }

    fun updateResupplyPoint(point: ResupplyPoint) = viewModelScope.launch(Dispatchers.IO) {
        repo.updateResupplyPoint(point)
    }

    fun deleteResupplyPoint(point: ResupplyPoint) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteResupplyPoint(point)
    }

    fun recommendations(trip: Trip): List<GearRecommendation> =
        recommendationEngine.recommendations(TripConditions.from(trip))
}
