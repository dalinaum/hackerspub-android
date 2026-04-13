package pub.hackers.android.ui.screens.recommendedactors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pub.hackers.android.data.repository.HackersPubRepository
import pub.hackers.android.domain.model.Actor
import javax.inject.Inject

data class RecommendedActorsUiState(
    val displayedActors: List<Actor> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RecommendedActorsViewModel @Inject constructor(
    private val repository: HackersPubRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendedActorsUiState())
    val uiState: StateFlow<RecommendedActorsUiState> = _uiState.asStateFlow()

    private val bufferQueue = mutableListOf<Actor>()
    private val seenActorIds = mutableSetOf<String>()

    init {
        loadInitialBatch()
    }

    private fun loadInitialBatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getRecommendedActors(limit = FETCH_SIZE).fold(
                onSuccess = { actors ->
                    val newActors = actors.filter { it.id !in seenActorIds }
                    bufferQueue.addAll(newActors)
                    updateDisplayedActors()
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun dismissActor(actorId: String) {
        seenActorIds.add(actorId)
        bufferQueue.removeAll { it.id == actorId }
        updateDisplayedActors()
        if (bufferQueue.size <= REFETCH_THRESHOLD) {
            fetchMoreActors()
        }
    }

    fun followActor(actorId: String) {
        viewModelScope.launch {
            repository.followActor(actorId).fold(
                onSuccess = {
                    seenActorIds.add(actorId)
                    bufferQueue.removeAll { it.id == actorId }
                    updateDisplayedActors()
                    if (bufferQueue.size <= REFETCH_THRESHOLD) {
                        fetchMoreActors()
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    private fun fetchMoreActors() {
        viewModelScope.launch {
            repository.getRecommendedActors(limit = FETCH_SIZE).fold(
                onSuccess = { actors ->
                    val newActors = actors.filter { it.id !in seenActorIds }
                    bufferQueue.addAll(newActors)
                    updateDisplayedActors()
                },
                onFailure = { /* silently fail for background fetch */ }
            )
        }
    }

    private fun updateDisplayedActors() {
        _uiState.update { it.copy(displayedActors = bufferQueue.take(DISPLAY_COUNT)) }
    }

    companion object {
        private const val DISPLAY_COUNT = 6
        private const val FETCH_SIZE = 10
        private const val REFETCH_THRESHOLD = 5
    }
}
