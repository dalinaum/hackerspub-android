package pub.hackers.android.ui.screens.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pub.hackers.android.data.repository.HackersPubRepository
import javax.inject.Inject

data class ComposeArticleUiState(
    val title: String = "",
    val content: String = "",
    val tags: String = "",
    val draftId: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ComposeArticleViewModel @Inject constructor(
    private val repository: HackersPubRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComposeArticleUiState())
    val uiState: StateFlow<ComposeArticleUiState> = _uiState.asStateFlow()

    fun loadDraft(draftId: String) {
        viewModelScope.launch {
            repository.getArticleDraft(draftId)
                .onSuccess { draft ->
                    _uiState.update {
                        it.copy(
                            title = draft.title,
                            content = draft.content,
                            tags = draft.tags.joinToString(", "),
                            draftId = draft.id
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun updateTags(tags: String) {
        _uiState.update { it.copy(tags = tags) }
    }

    fun saveDraft() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }
        if (state.isSaving) return

        val tagsList = state.tags
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, isSaved = false) }

            repository.saveArticleDraft(
                title = state.title,
                content = state.content,
                tags = tagsList,
                id = state.draftId
            )
                .onSuccess { draft ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true,
                            draftId = draft.id
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message,
                            isSaving = false
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSavedFlag() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
