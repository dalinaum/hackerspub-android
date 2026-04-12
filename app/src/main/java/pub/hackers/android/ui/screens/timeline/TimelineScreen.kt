package pub.hackers.android.ui.screens.timeline

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import pub.hackers.android.R
import pub.hackers.android.ui.components.ErrorMessage
import pub.hackers.android.ui.components.FullScreenLoading
import pub.hackers.android.ui.components.LargeTitleHeader
import pub.hackers.android.ui.components.LoadingItem
import pub.hackers.android.ui.components.PostCard
import pub.hackers.android.ui.components.ReactionPicker
import pub.hackers.android.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onComposeClick: (String?) -> Unit,
    onQuoteClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit,
    postedAt: Long = 0L,
    tabRetapped: Long = 0L,
    userAvatarUrl: String? = null,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val items = viewModel.posts.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val colors = LocalAppColors.current

    // After composing a new post, refresh and scroll to top once refresh completes.
    LaunchedEffect(postedAt) {
        if (postedAt > 0L) {
            items.refresh()
            snapshotFlow { items.loadState.refresh }
                .dropWhile { it !is LoadState.Loading }
                .first { it !is LoadState.Loading }
            listState.scrollToItem(0)
        }
    }

    // Home tab re-tap: if at top, refresh; otherwise scroll to top.
    LaunchedEffect(tabRetapped) {
        if (tabRetapped > 0L) {
            if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                items.refresh()
            } else {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Reaction picker bottom sheet — look up the currently-loaded post by id.
    val pickerPostId = uiState.reactionPickerPostId
    if (pickerPostId != null) {
        val pickerPost = items.itemSnapshotList.items.find {
            it.id == pickerPostId || it.sharedPost?.id == pickerPostId
        }
        val targetPost = pickerPost?.sharedPost ?: pickerPost
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideReactionPicker() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ReactionPicker(
                reactionGroups = targetPost?.reactionGroups ?: emptyList(),
                isSubmitting = false,
                onEmojiSelect = { emoji ->
                    pickerPost?.let { viewModel.toggleReaction(it, emoji) }
                },
                onClose = { viewModel.hideReactionPicker() }
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeTitleHeader(title = stringResource(R.string.personal_timeline)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color = colors.surface, shape = CircleShape)
                        .clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.nav_settings),
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (userAvatarUrl != null) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onComposeClick(null) },
                containerColor = colors.composeAccent,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.compose)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val refresh = items.loadState.refresh
            when {
                refresh is LoadState.Loading && items.itemCount == 0 -> {
                    FullScreenLoading()
                }

                refresh is LoadState.Error && items.itemCount == 0 -> {
                    ErrorMessage(
                        message = refresh.error.message ?: stringResource(R.string.error_generic),
                        onRetry = { items.refresh() }
                    )
                }

                refresh is LoadState.NotLoading && items.itemCount == 0 -> {
                    ErrorMessage(message = stringResource(R.string.no_posts))
                }

                else -> {
                    PullToRefreshBox(
                        isRefreshing = refresh is LoadState.Loading,
                        onRefresh = { items.refresh() }
                    ) {
                        LazyColumn(state = listState) {
                            items(
                                count = items.itemCount,
                                key = items.itemKey { it.id }
                            ) { index ->
                                val post = items[index] ?: return@items
                                PostCard(
                                    post = post,
                                    onClick = { onPostClick(post.sharedPost?.id ?: post.id) },
                                    onProfileClick = onProfileClick,
                                    onReplyClick = {
                                        onComposeClick(
                                            post.sharedPost?.id ?: post.id
                                        )
                                    },
                                    onShareClick = {
                                        if (post.viewerHasShared) {
                                            viewModel.unsharePost(post.id)
                                        } else {
                                            viewModel.sharePost(post.id)
                                        }
                                    },
                                    onQuoteClick = { onQuoteClick(post.sharedPost?.id ?: post.id) },
                                    onReactionClick = { viewModel.toggleFavourite(post) },
                                    onReactionLongPress = {
                                        viewModel.showReactionPicker(post.sharedPost?.id ?: post.id)
                                    },
                                    onExternalShareClick = {
                                        val displayPost = post.sharedPost ?: post
                                        val shareUrl = displayPost.url ?: displayPost.iri
                                        if (shareUrl != null) {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareUrl)
                                                type = "text/plain"
                                            }
                                            context.startActivity(
                                                Intent.createChooser(
                                                    sendIntent,
                                                    null
                                                )
                                            )
                                        }
                                    },
                                    onQuotedPostClick = onPostClick
                                )
                                HorizontalDivider(
                                    color = colors.divider,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            if (items.loadState.append is LoadState.Loading) {
                                item { LoadingItem() }
                            }
                        }
                    }
                }
            }
        }
    }
}
