package pub.hackers.android.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pub.hackers.android.data.paging.PostOverlayStore
import pub.hackers.android.data.paging.applyOverlays
import pub.hackers.android.data.paging.cursorPager
import pub.hackers.android.data.paging.distinctByEffectiveId
import pub.hackers.android.data.paging.localTimelinePage
import pub.hackers.android.data.paging.publicTimelinePage
import pub.hackers.android.data.repository.HackersPubRepository
import pub.hackers.android.domain.model.Post
import pub.hackers.android.domain.model.ReactionGroup
import javax.inject.Inject

enum class ExploreTab {
    LOCAL, GLOBAL
}

data class ExploreUiState(
    val selectedTab: ExploreTab = ExploreTab.LOCAL,
    val error: String? = null,
    val reactionPickerPostId: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: HackersPubRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val overlayStore = PostOverlayStore()
    private val selectedTab = MutableStateFlow(ExploreTab.LOCAL)

    val posts: Flow<PagingData<Post>> = combine(
        selectedTab.flatMapLatest { tab ->
            when (tab) {
                ExploreTab.LOCAL ->
                    cursorPager { after -> repository.localTimelinePage(after) }.flow

                ExploreTab.GLOBAL ->
                    cursorPager { after -> repository.publicTimelinePage(after) }.flow
            }.distinctByEffectiveId().cachedIn(viewModelScope)
        },
        overlayStore.overlays,
    ) { paging, overlays ->
        paging.map { post -> post.applyOverlays(overlays) }
    }.cachedIn(viewModelScope)

    fun selectTab(tab: ExploreTab) {
        if (tab != _uiState.value.selectedTab) {
            _uiState.update { it.copy(selectedTab = tab) }
            selectedTab.value = tab
        }
    }

    fun sharePost(postId: String) {
        overlayStore.mutate(postId) {
            it.copy(viewerHasShared = true, shareDelta = it.shareDelta + 1)
        }
        viewModelScope.launch {
            repository.sharePost(postId).onFailure {
                overlayStore.mutate(postId) { prev ->
                    prev.copy(viewerHasShared = false, shareDelta = prev.shareDelta - 1)
                }
            }
        }
    }

    fun unsharePost(postId: String) {
        overlayStore.mutate(postId) {
            it.copy(viewerHasShared = false, shareDelta = it.shareDelta - 1)
        }
        viewModelScope.launch {
            repository.unsharePost(postId).onFailure {
                overlayStore.mutate(postId) { prev ->
                    prev.copy(viewerHasShared = true, shareDelta = prev.shareDelta + 1)
                }
            }
        }
    }

    fun toggleFavourite(post: Post) {
        toggleReaction(post, "❤️")
    }

    fun toggleReaction(post: Post, emoji: String) {
        val target = post.sharedPost ?: post
        val existing = target.reactionGroups.find { it.emoji == emoji }
        val wasReacted = existing?.viewerHasReacted == true

        val updatedGroups = computeToggledReactionGroups(target.reactionGroups, emoji, wasReacted)

        overlayStore.mutate(target.id) { prev ->
            prev.copy(
                reactionOverride = updatedGroups,
                reactionCountOverride = updatedGroups.sumOf { it.count },
            )
        }
        _uiState.update { it.copy(reactionPickerPostId = null) }

        viewModelScope.launch {
            val result = if (wasReacted) {
                repository.removeReactionFromPost(target.id, emoji)
            } else {
                repository.addReactionToPost(target.id, emoji)
            }
            result.onFailure { overlayStore.clear(target.id) }
        }
    }

    fun showReactionPicker(postId: String) {
        _uiState.update { it.copy(reactionPickerPostId = postId) }
    }

    fun hideReactionPicker() {
        _uiState.update { it.copy(reactionPickerPostId = null) }
    }

    private fun computeToggledReactionGroups(
        groups: List<ReactionGroup>,
        emoji: String,
        wasReacted: Boolean,
    ): List<ReactionGroup> = if (wasReacted) {
        groups.map { g ->
            if (g.emoji == emoji) g.copy(count = maxOf(0, g.count - 1), viewerHasReacted = false)
            else g
        }.filter { it.count > 0 || it.viewerHasReacted }
    } else {
        val existing = groups.find { it.emoji == emoji }
        if (existing != null) {
            groups.map { g ->
                if (g.emoji == emoji) g.copy(count = g.count + 1, viewerHasReacted = true)
                else g
            }
        } else {
            groups + ReactionGroup(
                emoji = emoji, customEmoji = null,
                count = 1, reactors = emptyList(), viewerHasReacted = true,
            )
        }
    }
}
