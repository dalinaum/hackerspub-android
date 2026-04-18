package pub.hackers.android.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pub.hackers.android.data.local.PreferencesManager
import pub.hackers.android.data.repository.HackersPubRepository
import pub.hackers.android.domain.model.Actor
import pub.hackers.android.domain.model.Post
import javax.inject.Inject

enum class SearchMode { ALL, PEOPLE, POSTS, TAGS }

data class SearchUiState(
    val query: String = "",
    val mode: SearchMode = SearchMode.ALL,
    val actors: List<Actor> = emptyList(),
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null,
    val resolvedObjectUrl: String? = null,
    val recentSearches: List<String> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: HackersPubRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.recentSearches.collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun setMode(mode: SearchMode) {
        if (_uiState.value.mode == mode) return
        _uiState.update { it.copy(mode = mode) }
        if (_uiState.value.hasSearched && _uiState.value.query.isNotBlank()) {
            search()
        }
    }

    fun search() {
        val rawQuery = _uiState.value.query.trim()
        if (rawQuery.isEmpty()) return
        val mode = _uiState.value.mode

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    hasSearched = true,
                    resolvedObjectUrl = null,
                    actors = emptyList(),
                    posts = emptyList()
                )
            }

            preferencesManager.addRecentSearch(rawQuery)

            repository.searchObject(rawQuery)
                .onSuccess { url ->
                    if (url != null) {
                        _uiState.update { it.copy(resolvedObjectUrl = url) }
                    }
                }

            when (mode) {
                SearchMode.ALL -> {
                    val handleQuery = rawQuery.removePrefix("@")
                    repository.searchActorsByHandle(handleQuery, limit = 5)
                        .onSuccess { actors ->
                            _uiState.update { it.copy(actors = actors) }
                        }
                    repository.searchPosts(rawQuery)
                        .onSuccess { posts ->
                            _uiState.update { it.copy(posts = posts, isLoading = false) }
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(error = error.message, isLoading = false) }
                        }
                }
                SearchMode.PEOPLE -> {
                    val handleQuery = rawQuery.removePrefix("@")
                    repository.searchActorsByHandle(handleQuery, limit = 30)
                        .onSuccess { actors ->
                            _uiState.update { it.copy(actors = actors, isLoading = false) }
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(error = error.message, isLoading = false) }
                        }
                }
                SearchMode.POSTS, SearchMode.TAGS -> {
                    val postQuery = if (mode == SearchMode.TAGS && !rawQuery.startsWith("#")) {
                        "#$rawQuery"
                    } else {
                        rawQuery
                    }
                    repository.searchPosts(postQuery)
                        .onSuccess { posts ->
                            _uiState.update { it.copy(posts = posts, isLoading = false) }
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(error = error.message, isLoading = false) }
                        }
                }
            }
        }
    }

    fun consumeResolvedUrl() {
        _uiState.update { it.copy(resolvedObjectUrl = null) }
    }

    fun clearSearch() {
        _uiState.update {
            SearchUiState(recentSearches = it.recentSearches, mode = it.mode)
        }
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            preferencesManager.removeRecentSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            preferencesManager.clearRecentSearches()
        }
    }

    fun selectRecentSearch(query: String) {
        _uiState.update { it.copy(query = query) }
        search()
    }
}
