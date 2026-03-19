package pub.hackers.android.ui.screens.postdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import pub.hackers.android.ui.components.LargeTitleHeader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import pub.hackers.android.R
import pub.hackers.android.domain.model.Post
import pub.hackers.android.domain.model.ReactionGroup
import pub.hackers.android.ui.components.ErrorMessage
import pub.hackers.android.ui.components.FullScreenLoading
import pub.hackers.android.ui.components.HtmlContent
import pub.hackers.android.ui.components.MediaGrid
import pub.hackers.android.ui.components.PostCard
import pub.hackers.android.ui.components.QuotedPostPreview
import pub.hackers.android.ui.theme.AppShapes
import pub.hackers.android.ui.theme.LocalAppColors
import pub.hackers.android.ui.theme.LocalAppTypography
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    onReplyClick: (String) -> Unit,
    onQuoteClick: (String) -> Unit = {},
    onPostClick: (String) -> Unit,
    isLoggedIn: Boolean = false,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeTitleHeader(
                title = "Post",
                leadingContent = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.accent
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.post != null) {
                FloatingActionButton(
                    onClick = { onReplyClick(postId) },
                    containerColor = colors.accent,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = stringResource(R.string.reply)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    FullScreenLoading()
                }
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error ?: stringResource(R.string.error_generic),
                        onRetry = { viewModel.loadPost(postId) }
                    )
                }
                uiState.post != null -> {
                    PostDetailContent(
                        post = uiState.post!!,
                        reactionGroups = uiState.reactionGroups,
                        replies = uiState.replies,
                        onProfileClick = onProfileClick,
                        onPostClick = onPostClick,
                        onShareClick = {
                            if (uiState.post!!.viewerHasShared) {
                                viewModel.unsharePost()
                            } else {
                                viewModel.sharePost()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PostDetailContent(
    post: Post,
    reactionGroups: List<ReactionGroup>,
    replies: List<Post>,
    onProfileClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onShareClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
        .withZone(ZoneId.systemDefault())

    LazyColumn {
        item {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Reply target preview
                if (post.replyTarget != null) {
                    ReplyTargetPreview(
                        post = post.replyTarget!!,
                        onClick = { onPostClick(post.replyTarget!!.id) },
                        onProfileClick = onProfileClick
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.divider)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Author row: 42dp avatar, bodyLargeSemiBold name
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = post.actor.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(AppShapes.avatarTimeline)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = post.actor.name ?: post.actor.handle,
                            style = typography.bodyLargeSemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "@${post.actor.handle}",
                            style = typography.labelMedium,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                post.name?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Body text in textBody color
                HtmlContent(
                    html = post.content,
                    modifier = Modifier.fillMaxWidth(),
                    onMentionClick = onProfileClick
                )

                if (post.media.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MediaGrid(media = post.media)
                }

                if (post.quotedPost != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    QuotedPostPreview(
                        post = post.quotedPost!!,
                        onClick = { onPostClick(post.quotedPost!!.id) },
                        onProfileClick = onProfileClick
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timestamp: labelMedium + textSecondary
                Text(
                    text = dateFormatter.format(post.published),
                    style = typography.labelMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Engagement stats row: count bold + textPrimary, labels + textSecondary
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.textPrimary)) {
                                append("${post.engagementStats.replies}")
                            }
                            append(" ")
                            withStyle(SpanStyle(color = colors.textSecondary)) {
                                append(stringResource(R.string.replies))
                            }
                        },
                        style = typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.textPrimary)) {
                                append("${post.engagementStats.shares}")
                            }
                            append(" ")
                            withStyle(SpanStyle(color = colors.textSecondary)) {
                                append(stringResource(R.string.shares))
                            }
                        },
                        style = typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.textPrimary)) {
                                append("${post.engagementStats.reactions}")
                            }
                            append(" ")
                            withStyle(SpanStyle(color = colors.textSecondary)) {
                                append(stringResource(R.string.reactions))
                            }
                        },
                        style = typography.bodyMedium
                    )
                }

                // Reaction groups
                if (reactionGroups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        reactionGroups.forEach { group ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colors.surface,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (group.emoji != null) {
                                        Text(text = group.emoji)
                                    } else if (group.customEmoji != null) {
                                        AsyncImage(
                                            model = group.customEmoji.imageUrl,
                                            contentDescription = group.customEmoji.name,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = group.count.toString(),
                                        style = typography.bodyMedium,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action bar with dividers
                HorizontalDivider(color = colors.divider)

                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = stringResource(R.string.share),
                            tint = if (post.viewerHasShared)
                                colors.share
                            else
                                colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = colors.divider)
            }
        }

        if (replies.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.replies),
                    style = typography.bodyLargeSemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            items(
                items = replies,
                key = { it.id }
            ) { reply ->
                PostCard(
                    post = reply,
                    onClick = { onPostClick(reply.id) },
                    onProfileClick = onProfileClick,
                    onQuotedPostClick = onPostClick
                )
                HorizontalDivider(thickness = 0.5.dp, color = colors.divider)
            }
        }
    }
}

@Composable
private fun ReplyTargetPreview(
    post: Post,
    onClick: () -> Unit,
    onProfileClick: (String) -> Unit
) {
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colors.divider,
                shape = RoundedCornerShape(AppShapes.quotedPostRadius)
            )
            .clip(RoundedCornerShape(AppShapes.quotedPostRadius))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(8.dp)
            .alpha(0.6f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.actor.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick(post.actor.handle) },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = post.actor.name ?: post.actor.handle,
                    style = typography.bodyLargeSemiBold,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = "@${post.actor.handle}",
                    style = typography.labelMedium,
                    color = colors.textSecondary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HtmlContent(
            html = post.content,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            onMentionClick = onProfileClick
        )

        if (post.media.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            MediaGrid(media = post.media)
        }
    }
}
