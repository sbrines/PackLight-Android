package com.stephenbrines.packlight.ui.gear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stephenbrines.packlight.data.model.GearCategory
import com.stephenbrines.packlight.data.model.GearItem
import com.stephenbrines.packlight.data.repository.GearRepository
import com.stephenbrines.packlight.service.GearMetadata
import com.stephenbrines.packlight.service.UrlMetadataFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GearSortOption(val label: String) {
    NAME_ASC("Name (A–Z)"),
    NAME_DESC("Name (Z–A)"),
    WEIGHT_LIGHT("Lightest First"),
    WEIGHT_HEAVY("Heaviest First"),
    RECENTLY_ADDED("Recently Added"),
}

data class GearUiState(
    val items: List<GearItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: GearCategory? = null,
    val sortOption: GearSortOption = GearSortOption.NAME_ASC,
    val isFetchingUrl: Boolean = false,
    val urlFetchError: String? = null,
    val fetchedMetadata: GearMetadata? = null,
)

@HiltViewModel
class GearViewModel @Inject constructor(
    private val repo: GearRepository,
    private val fetcher: UrlMetadataFetcher,
) : ViewModel() {

    private val _state = MutableStateFlow(GearUiState())
    val state: StateFlow<GearUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().collect { items ->
                _state.update { it.copy(items = items) }
            }
        }
    }

    val filteredItems: StateFlow<List<GearItem>> = _state.map { s ->
        var result = s.items
        s.selectedCategory?.let { cat -> result = result.filter { it.category == cat } }
        if (s.searchQuery.isNotBlank()) {
            result = result.filter {
                it.name.contains(s.searchQuery, ignoreCase = true) ||
                it.brand.contains(s.searchQuery, ignoreCase = true)
            }
        }
        when (s.sortOption) {
            GearSortOption.NAME_ASC -> result.sortedBy { it.name }
            GearSortOption.NAME_DESC -> result.sortedByDescending { it.name }
            GearSortOption.WEIGHT_LIGHT -> result.sortedBy { it.weightGrams }
            GearSortOption.WEIGHT_HEAVY -> result.sortedByDescending { it.weightGrams }
            GearSortOption.RECENTLY_ADDED -> result.sortedByDescending { it.createdAt }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearch(q: String) = _state.update { it.copy(searchQuery = q) }
    fun setCategory(cat: GearCategory?) = _state.update { it.copy(selectedCategory = cat) }
    fun setSort(opt: GearSortOption) = _state.update { it.copy(sortOption = opt) }

    fun saveItem(item: GearItem) = viewModelScope.launch(Dispatchers.IO) {
        repo.insert(item)
    }

    fun deleteItem(item: GearItem) = viewModelScope.launch(Dispatchers.IO) {
        repo.delete(item)
    }

    fun fetchFromUrl(urlString: String) = viewModelScope.launch(Dispatchers.IO) {
        _state.update { it.copy(isFetchingUrl = true, urlFetchError = null, fetchedMetadata = null) }
        val result = fetcher.fetch(urlString)
        _state.update {
            it.copy(
                isFetchingUrl = false,
                fetchedMetadata = result.getOrNull(),
                urlFetchError = result.exceptionOrNull()?.message,
            )
        }
    }

    fun clearFetchedMetadata() = _state.update { it.copy(fetchedMetadata = null, urlFetchError = null) }
}
